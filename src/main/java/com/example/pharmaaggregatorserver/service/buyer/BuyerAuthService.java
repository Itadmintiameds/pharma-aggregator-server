package com.example.pharmaaggregatorserver.service.buyer;

import com.example.pharmaaggregatorserver.dto.buyer.BuyerLoginRequest;
import com.example.pharmaaggregatorserver.dto.buyer.BuyerLoginResponse;
import com.example.pharmaaggregatorserver.dto.buyer.BuyerOtpSentResponse;
import com.example.pharmaaggregatorserver.dto.buyer.BuyerOtpVerificationRequest;
import com.example.pharmaaggregatorserver.dto.buyer.BuyerUserSummaryDTO;
import com.example.pharmaaggregatorserver.entity.buyer.BuyerLoginOtp;
import com.example.pharmaaggregatorserver.entity.buyer.BuyerRefreshToken;
import com.example.pharmaaggregatorserver.entity.buyer.BuyerUser;
import com.example.pharmaaggregatorserver.exception.*;
import com.example.pharmaaggregatorserver.exception.auth.OtpExpiredException;
import com.example.pharmaaggregatorserver.exception.auth.OtpInvalidException;
import com.example.pharmaaggregatorserver.exception.auth.OtpLockedException;
import com.example.pharmaaggregatorserver.exception.auth.RefreshTokenException;
import com.example.pharmaaggregatorserver.repository.buyer.BuyerLoginOtpRepository;
import com.example.pharmaaggregatorserver.repository.buyer.BuyerRefreshTokenRepository;
import com.example.pharmaaggregatorserver.repository.buyer.BuyerUserRepository;
import com.example.pharmaaggregatorserver.security.JwtUtils;
import com.example.pharmaaggregatorserver.security.UserDetailsImpl;
import com.example.pharmaaggregatorserver.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Buyer login mirrors the seller's password → email-OTP → JWT flow, but is
 * fully isolated: backed by BuyerUser/tbl_buyer_user (never tbl_user), and
 * does NOT go through Spring Security's AuthenticationManager (that bean is
 * bound to UserDetailsServiceImpl → UserRepository → tbl_user). Instead,
 * password verification is a manual PasswordEncoder.matches() check, and the
 * Authentication handed to JwtUtils is built directly from a manually
 * constructed UserDetailsImpl (JwtUtils.generateJwtToken hard-casts the
 * principal to UserDetailsImpl, so it must actually be one).
 */
@Service
@RequiredArgsConstructor
public class BuyerAuthService {

    private final BuyerRefreshTokenRepository buyerRefreshTokenRepository;
    private final BuyerUserRepository buyerUserRepository;
    private final BuyerLoginOtpRepository buyerLoginOtpRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final EmailService emailService;

    @Value("${app.jwt.refresh-expiration}")
    private long refreshTokenExpirationMs;

    private static final int MAX_LOGIN_FAILED_ATTEMPTS = 5;
    private static final int MAX_OTP_FAILED_ATTEMPTS = 3;
    private static final int OTP_EXPIRY_MINUTES = 5;

    // ─────────────────────────────────────────────────────────
    // STEP 1 — Validate credentials → send OTP
    // ─────────────────────────────────────────────────────────

    @Transactional
    public BuyerOtpSentResponse validateCredentialsAndSendOtp(BuyerLoginRequest loginRequest) {

        BuyerUser buyerUser = buyerUserRepository.findByEmail(loginRequest.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        if (buyerUser.isAccountLocked()) {
            throw new AccountLockedException("Your account has been locked. Please contact support.");
        }
        if (!buyerUser.isActive()) {
            throw new AccountInactiveException("Your account is inactive. Please contact support.");
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), buyerUser.getPasswordHash())) {
            handleFailedLogin(buyerUser);
            throw new InvalidCredentialsException("Invalid username or password");
        }

        resetFailedLoginAttempts(buyerUser);

        buyerLoginOtpRepository.invalidateAllOtpsForBuyerUser(buyerUser);

        String otpCode = generateOtp();

        BuyerLoginOtp loginOtp = BuyerLoginOtp.builder()
                .buyerUser(buyerUser)
                .otpCode(otpCode)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .isUsed(false)
                .failedAttempts(0)
                .isLocked(false)
                .build();
        buyerLoginOtpRepository.save(loginOtp);

        emailService.sendBuyerOtp(buyerUser.getEmail(), otpCode);

        return BuyerOtpSentResponse.builder()
                .message("OTP sent to registered email. Valid for " + OTP_EXPIRY_MINUTES + " minutes.")
                .username(buyerUser.getEmail())
                .build();
    }

    // ─────────────────────────────────────────────────────────
    // STEP 2 — Validate OTP → issue JWT
    // ─────────────────────────────────────────────────────────

    @Transactional
    public BuyerLoginResponse verifyOtpAndIssueToken(BuyerOtpVerificationRequest request) {

        BuyerUser buyerUser = buyerUserRepository.findByEmail(request.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        if (buyerUser.isAccountLocked()) {
            throw new AccountLockedException("Your account has been locked. Please contact support.");
        }
        if (!buyerUser.isActive()) {
            throw new AccountInactiveException("Your account is inactive. Please contact support.");
        }

        BuyerLoginOtp loginOtp = buyerLoginOtpRepository.findActiveOtpByBuyerUser(buyerUser)
                .orElseThrow(() -> new OtpExpiredException("OTP has expired or is invalid. Please login again."));

        if (!loginOtp.getOtpCode().equals(request.getOtp())) {
            handleFailedOtp(loginOtp);
            int newAttempts = loginOtp.getFailedAttempts() + 1;
            if (newAttempts >= MAX_OTP_FAILED_ATTEMPTS) {
                throw new OtpLockedException("Too many wrong OTP attempts. Please login again to get a new OTP.");
            }
            int remaining = MAX_OTP_FAILED_ATTEMPTS - newAttempts;
            throw new OtpInvalidException("Invalid OTP. " + remaining + " attempt(s) remaining.");
        }

        buyerLoginOtpRepository.markOtpAsUsed(loginOtp.getOtpId());
        buyerUserRepository.updateLastLogin(buyerUser.getBuyerUserId(), LocalDateTime.now());

        return issueTokensForUser(buyerUser);
    }

    /**
     * Builds a Spring Security Authentication manually (no AuthenticationManager
     * involved) and issues a fresh access token + persisted refresh token.
     */
    @Transactional
    public BuyerLoginResponse issueTokensForUser(BuyerUser buyerUser) {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_BUYER"));

        UserDetailsImpl userDetails = UserDetailsImpl.builder()
                .id(buyerUser.getBuyerUserId())
                .username(buyerUser.getEmail())
                .password(buyerUser.getPasswordHash())
                .isPasswordTemporary(buyerUser.isPasswordTemporary())
                .isActive(buyerUser.isActive())
                .isAccountLocked(buyerUser.isAccountLocked())
                .authorities(authorities)
                .build();

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        String accessToken = jwtUtils.generateJwtToken(authentication);
        String rawRefreshToken = jwtUtils.generateRefreshToken();

        var roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        BuyerRefreshToken refreshToken = BuyerRefreshToken.builder()
                .buyerUser(buyerUser)
                .tokenHash(jwtUtils.hashToken(rawRefreshToken))
                .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpirationMs / 1000))
                .build();
        buyerRefreshTokenRepository.save(refreshToken);

        return BuyerLoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .buyerUserId(userDetails.getId())
                .username(userDetails.getUsername())
                .phone(buyerUser.getPhone())
                .roles(roles)
                .passwordTemporary(buyerUser.isPasswordTemporary())
                .message("Login successful")
                .build();
    }

    // ─────────────────────────────────────────────────────────
    // First-time password reset (buyer account auto-provisioned with a
    // temporary password via a guest quote-request submission). Mirrors
    // seller's UserService.resetPassword — only usable while
    // isPasswordTemporary is still true.
    // ─────────────────────────────────────────────────────────

    @Transactional
    public void resetPassword(String email, String newPassword) {
        BuyerUser buyerUser = buyerUserRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Buyer account not found"));

        if (!buyerUser.isPasswordTemporary()) {
            throw new ApplicationException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "You have already set your password. Please use the regular login."
            );
        }

        if (buyerUser.isAccountLocked()) {
            throw new AccountLockedException("Your account has been locked. Please contact support.");
        }
        if (!buyerUser.isActive()) {
            throw new AccountInactiveException("Your account is inactive. Please contact support.");
        }

        buyerUser.setPasswordHash(passwordEncoder.encode(newPassword));
        buyerUser.setPasswordTemporary(false);
        buyerUser.setFailedLoginAttempts(0);
        buyerUserRepository.save(buyerUser);
    }

    // ─────────────────────────────────────────────────────────
    // "Who am I" — lets the frontend refresh email/phone on demand (e.g. the
    // buyer registration wizard's "same as my login email/mobile" checkboxes)
    // instead of only trusting whatever BuyerLoginResponse was cached at the
    // last login/OTP-verify.
    // ─────────────────────────────────────────────────────────

    public BuyerUserSummaryDTO getCurrentUserSummary(Long buyerUserId) {
        BuyerUser buyerUser = buyerUserRepository.findById(buyerUserId)
                .orElseThrow(() -> new InvalidCredentialsException("Buyer account not found"));

        return BuyerUserSummaryDTO.builder()
                .buyerUserId(buyerUser.getBuyerUserId())
                .email(buyerUser.getEmail())
                .phone(buyerUser.getPhone())
                .build();
    }

    // ─────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────

    private void handleFailedLogin(BuyerUser buyerUser) {
        int newAttempts = buyerUser.getFailedLoginAttempts() + 1;
        buyerUserRepository.updateFailedLoginAttempts(buyerUser.getBuyerUserId(), newAttempts);
        if (newAttempts >= MAX_LOGIN_FAILED_ATTEMPTS) {
            buyerUserRepository.updateAccountLocked(buyerUser.getBuyerUserId(), true);
        }
    }

    private void resetFailedLoginAttempts(BuyerUser buyerUser) {
        if (buyerUser.getFailedLoginAttempts() > 0) {
            buyerUserRepository.updateFailedLoginAttempts(buyerUser.getBuyerUserId(), 0);
        }
    }

    private void handleFailedOtp(BuyerLoginOtp loginOtp) {
        buyerLoginOtpRepository.incrementFailedAttempts(loginOtp.getOtpId());
        int newAttempts = loginOtp.getFailedAttempts() + 1;
        if (newAttempts >= MAX_OTP_FAILED_ATTEMPTS) {
            buyerLoginOtpRepository.lockOtp(loginOtp.getOtpId());
        }
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    @Transactional
    public BuyerLoginResponse refreshAccessToken(String rawRefreshToken) {
        String hash = jwtUtils.hashToken(rawRefreshToken);

        BuyerRefreshToken stored = buyerRefreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new RefreshTokenException("Invalid refresh token"));

        if (!stored.isValid()) {
            throw new RefreshTokenException("Refresh token expired or revoked. Please login again.");
        }

        stored.setRevokedAt(LocalDateTime.now());
        buyerRefreshTokenRepository.save(stored);

        BuyerUser buyerUser = stored.getBuyerUser();
        return issueTokensForUser(buyerUser);
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        String hash = jwtUtils.hashToken(rawRefreshToken);
        buyerRefreshTokenRepository.findByTokenHash(hash)
                .ifPresent(token -> {
                    token.setRevokedAt(LocalDateTime.now());
                    buyerRefreshTokenRepository.save(token);
                });
    }
}
