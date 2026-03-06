package com.example.pharmaaggregatorserver.service.seller.SellerLogIn;

import com.example.pharmaaggregatorserver.dto.seller.SellerLogIn.LoginRequest;
import com.example.pharmaaggregatorserver.dto.seller.SellerLogIn.LoginResponse;
import com.example.pharmaaggregatorserver.entity.auth.User;
import com.example.pharmaaggregatorserver.exception.AccountInactiveException;
import com.example.pharmaaggregatorserver.exception.AccountLockedException;
import com.example.pharmaaggregatorserver.exception.InvalidCredentialsException;
import com.example.pharmaaggregatorserver.repository.auth.UserRepository;
import com.example.pharmaaggregatorserver.security.JwtUtils;
import com.example.pharmaaggregatorserver.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    private static final int MAX_FAILED_ATTEMPTS = 5;

    @Transactional
    public LoginResponse authenticateUser(LoginRequest loginRequest) {
        try {
            // Check if user exists
            User user = userRepository.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

            // Check if account is locked
            if (user.isAccountLocked()) {
                throw new AccountLockedException("Your account has been locked. Please contact administrator.");
            }

            // Check if account is active
            if (!user.isActive()) {
                throw new AccountInactiveException("Your account is inactive. Please contact administrator.");
            }

            // Attempt authentication
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            // Reset failed login attempts on successful login
            resetFailedAttempts(user);

            // Update last login time
            updateLastLogin(user);

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            // Get roles
            var roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());

            return LoginResponse.builder()
                    .token(jwt)
                    .userId(userDetails.getId())
                    .username(userDetails.getUsername())
                    .roles(roles)
                    .passwordTemporary(userDetails.isPasswordTemporary())
                    .message("Login successful")
                    .build();

        } catch (AuthenticationException e) {
            // Handle failed login
            handleFailedLogin(loginRequest.getUsername());
            throw new InvalidCredentialsException("Invalid username or password");
        }
    }

    private void handleFailedLogin(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            int newAttempts = user.getFailedLoginAttempts() + 1;
            userRepository.updateFailedLoginAttempts(user.getUserId(), newAttempts);

            // Lock account if max attempts exceeded
            if (newAttempts >= MAX_FAILED_ATTEMPTS) {
                userRepository.updateAccountLocked(user.getUserId(), true);
            }
        });
    }

    private void resetFailedAttempts(User user) {
        if (user.getFailedLoginAttempts() > 0) {
            userRepository.updateFailedLoginAttempts(user.getUserId(), 0);
        }
    }

    private void updateLastLogin(User user) {
        userRepository.updateLastLogin(user.getUserId(), LocalDateTime.now());
    }
}
