package com.example.pharmaaggregatorserver.service.seller.SellerLogIn;

import com.example.pharmaaggregatorserver.dto.seller.SellerLogIn.LoginRequest;
import com.example.pharmaaggregatorserver.dto.seller.SellerLogIn.LoginResponse;
import com.example.pharmaaggregatorserver.dto.seller.SellerLogIn.OtpSentResponse;
import com.example.pharmaaggregatorserver.dto.seller.SellerLogIn.OtpVerificationRequest;
import com.example.pharmaaggregatorserver.entity.auth.LoginOtp;
import com.example.pharmaaggregatorserver.entity.auth.RefreshToken;
import com.example.pharmaaggregatorserver.entity.auth.User;
import com.example.pharmaaggregatorserver.exception.*;
import com.example.pharmaaggregatorserver.exception.auth.OtpExpiredException;
import com.example.pharmaaggregatorserver.exception.auth.OtpInvalidException;
import com.example.pharmaaggregatorserver.exception.auth.OtpLockedException;
import com.example.pharmaaggregatorserver.exception.auth.RefreshTokenException;
import com.example.pharmaaggregatorserver.repository.auth.LoginOtpRepository;
import com.example.pharmaaggregatorserver.repository.auth.UserRepository;
import com.example.pharmaaggregatorserver.repository.seller.SellerLogIn.refreshTokenRepository;
import com.example.pharmaaggregatorserver.security.JwtUtils;
import com.example.pharmaaggregatorserver.security.UserDetailsImpl;
import com.example.pharmaaggregatorserver.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final refreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final LoginOtpRepository loginOtpRepository;
    private final JwtUtils jwtUtils;
    private final EmailService emailService;

    @Value("${app.jwt.refresh-expiration}")
    private long refreshTokenExpirationMs;

    /** Max wrong password attempts before account is locked */
    private static final int MAX_LOGIN_FAILED_ATTEMPTS = 5;

    /** Max wrong OTP attempts before OTP entry is locked */
    private static final int MAX_OTP_FAILED_ATTEMPTS = 3;

    /** OTP validity window in minutes */
    private static final int OTP_EXPIRY_MINUTES = 5;

    // ─────────────────────────────────────────────────────────
    // STEP 1 — Validate credentials → send OTP
    // ─────────────────────────────────────────────────────────

    @Transactional
    public OtpSentResponse validateCredentialsAndSendOtp(LoginRequest loginRequest) {

        // 1. Find user
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        // 2. Pre-auth guard checks
        if (user.isAccountLocked()) {
            throw new AccountLockedException("Your account has been locked. Please contact administrator.");
        }
        if (!user.isActive()) {
            throw new AccountInactiveException("Your account is inactive. Please contact administrator.");
        }

        // 3. Authenticate credentials via Spring Security (BCrypt check happens here)
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(), loginRequest.getPassword()));
        } catch (AuthenticationException e) {
            handleFailedLogin(loginRequest.getUsername());
            throw new InvalidCredentialsException("Invalid username or password");
        }

        // 4. Credentials are valid — reset any previous failed attempts
        resetFailedLoginAttempts(user);

        // 5. Invalidate all previous OTPs for this user
        loginOtpRepository.invalidateAllOtpsForUser(user);

        // 6. Generate a fresh 6-digit OTP
        String otpCode = generateOtp();

        // 7. Persist OTP record
        LoginOtp loginOtp = LoginOtp.builder()
                .user(user)
                .otpCode(otpCode)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .isUsed(false)
                .failedAttempts(0)
                .isLocked(false)
                .build();
        loginOtpRepository.save(loginOtp);

        // 8. Send OTP via email
        emailService.sendCoordinatorOtp(user.getUsername(), otpCode);

        return OtpSentResponse.builder()
                .message("OTP sent to registered email. Valid for " + OTP_EXPIRY_MINUTES + " minutes.")
                .username(user.getUsername())
                .build();
    }

    // ─────────────────────────────────────────────────────────
    // STEP 2 — Validate OTP → issue JWT
    // ─────────────────────────────────────────────────────────

    @Transactional
    public LoginResponse verifyOtpAndIssueToken(OtpVerificationRequest request) {

        // 1. Find user
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        // 2. Guard checks (account could have been locked between Step 1 and Step 2)
        if (user.isAccountLocked()) {
            throw new AccountLockedException("Your account has been locked. Please contact administrator.");
        }
        if (!user.isActive()) {
            throw new AccountInactiveException("Your account is inactive. Please contact administrator.");
        }

        // 3. Find active OTP (unused, unexpired, unlocked)
        LoginOtp loginOtp = loginOtpRepository.findActiveOtpByUser(user)
                .orElseThrow(() -> new OtpExpiredException("OTP has expired or is invalid. Please login again."));

        // 4. Check if OTP matches
        if (!loginOtp.getOtpCode().equals(request.getOtp())) {
            handleFailedOtp(loginOtp);
            // Check if we just locked it
            int newAttempts = loginOtp.getFailedAttempts() + 1;
            if (newAttempts >= MAX_OTP_FAILED_ATTEMPTS) {
                throw new OtpLockedException("Too many wrong OTP attempts. Please login again to get a new OTP.");
            }
            int remaining = MAX_OTP_FAILED_ATTEMPTS - newAttempts;
            throw new OtpInvalidException("Invalid OTP. " + remaining + " attempt(s) remaining.");
        }

        // 5. Mark OTP as used
        loginOtpRepository.markOtpAsUsed(loginOtp.getOtpId());

        // 6. Update last login timestamp
        userRepository.updateLastLogin(user.getUserId(), LocalDateTime.now());

        // 7-9. Build Authentication, generate JWT + refresh token
        return issueTokensForUser(user);
    }

    /**
     * Builds a Spring Security Authentication for the given user and issues a
     * fresh access token + persisted refresh token. Shared by the login-OTP
     * flow above and by the standalone signup flow (SignupService), which both
     * need to hand back a ready-to-use LoginResponse once a user is verified.
     */
    @Transactional
    public LoginResponse issueTokensForUser(User user) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                UserDetailsImpl.build(user), null,
                UserDetailsImpl.build(user).getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtUtils.generateJwtToken(authentication);
        String rawRefreshToken = jwtUtils.generateRefreshToken();

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        var roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(jwtUtils.hashToken(rawRefreshToken))
                .expiresAt(LocalDateTime.now().plusSeconds(
                        refreshTokenExpirationMs / 1000))
                .build();
        refreshTokenRepository.save(refreshToken);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)   // raw sent to client once; DB holds only hash
                .userId(userDetails.getId())
                .username(userDetails.getUsername())
                .roles(roles)
                .passwordTemporary(userDetails.isPasswordTemporary())
                .message("Login successful")
                .build();
    }

    // ─────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────

    /**
     * Increments wrong-password counter; locks account if threshold reached.
     */
    private void handleFailedLogin(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            int newAttempts = user.getFailedLoginAttempts() + 1;
            userRepository.updateFailedLoginAttempts(user.getUserId(), newAttempts);
            if (newAttempts >= MAX_LOGIN_FAILED_ATTEMPTS) {
                userRepository.updateAccountLocked(user.getUserId(), true);
            }
        });
    }

    /**
     * Resets wrong-password counter on successful credential check.
     */
    private void resetFailedLoginAttempts(User user) {
        if (user.getFailedLoginAttempts() > 0) {
            userRepository.updateFailedLoginAttempts(user.getUserId(), 0);
        }
    }

    /**
     * Increments OTP failed attempts and locks OTP if threshold reached.
     */
    private void handleFailedOtp(LoginOtp loginOtp) {
        loginOtpRepository.incrementFailedAttempts(loginOtp.getOtpId());
        int newAttempts = loginOtp.getFailedAttempts() + 1;
        if (newAttempts >= MAX_OTP_FAILED_ATTEMPTS) {
            loginOtpRepository.lockOtp(loginOtp.getOtpId());
        }
    }

    /**
     * Generates a cryptographically secure 6-digit numeric OTP.
     */
    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000); // always 6 digits
        return String.valueOf(otp);
    }

    @Transactional
    public LoginResponse refreshAccessToken(String rawRefreshToken) {
         String hash = jwtUtils.hashToken(rawRefreshToken);

         RefreshToken stored = refreshTokenRepository.findByTokenHash(hash)
                 .orElseThrow(() -> new RefreshTokenException("Invalid refresh token"));

        if (!stored.isValid()) {
            throw new RefreshTokenException("Refresh token expired or revoked. Please login again.");
        }

        // Rotate — revoke old, issue new
        stored.setRevokedAt(LocalDateTime.now());
        refreshTokenRepository.save(stored);

        User user = stored.getUser();
        Authentication auth = new UsernamePasswordAuthenticationToken(
                UserDetailsImpl.build(user), null,
                UserDetailsImpl.build(user).getAuthorities());

        String newAccessToken  = jwtUtils.generateJwtToken(auth);
        String newRawRefresh   = jwtUtils.generateRefreshToken();

        RefreshToken newRefreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(jwtUtils.hashToken(newRawRefresh))
                .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpirationMs / 1000))
                .build();
        refreshTokenRepository.save(newRefreshToken);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRawRefresh)
                .userId(user.getUserId())
                .username(user.getUsername())
                .build();
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        String hash = jwtUtils.hashToken(rawRefreshToken);
        refreshTokenRepository.findByTokenHash(hash)
                .ifPresent(token -> {
                    token.setRevokedAt(LocalDateTime.now());
                    refreshTokenRepository.save(token);
                });
    }

}