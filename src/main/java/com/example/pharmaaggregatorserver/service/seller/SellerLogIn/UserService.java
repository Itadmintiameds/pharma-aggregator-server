package com.example.pharmaaggregatorserver.service.seller.SellerLogIn;

// UserService.java

import com.example.pharmaaggregatorserver.entity.auth.User;
import com.example.pharmaaggregatorserver.exception.BadRequestException;
import com.example.pharmaaggregatorserver.exception.ResourceNotFoundException;
import com.example.pharmaaggregatorserver.repository.seller.SellerLogIn.SellerUserRepository;
import com.example.pharmaaggregatorserver.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final SellerUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetEmailService passwordResetEmailService;
    private final EmailService emailService;

    @Value("${app.reset-token-expiration-hours:1}")
    private int tokenExpirationHours;

    // Your existing reset password method
    @Transactional
    public void resetPassword(String username, String currentPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        // Check if this is a first-time login
        boolean isFirstTimeLogin = user.isPasswordTemporary() || user.getLastLoginAt() == null;

        log.debug("Password reset attempt for user: {}, isFirstTimeLogin: {}", username, isFirstTimeLogin);

        // ONLY allow first-time users to reset password through this method
        if (!isFirstTimeLogin) {
            // This is not a first-time login - user has already reset password before
            throw new BadRequestException("You have already set your password. Please use the regular password reset option or contact support if you forgot your password.");
        }

        // For first-time login, we don't need to verify current password
        log.info("First-time password reset for user: {}", username);

        // Check if account is locked
        if (user.isAccountLocked()) {
            throw new BadRequestException("Account is locked. Please contact support.");
        }

        // Check if user is active
        if (!user.isActive()) {
            throw new BadRequestException("Account is inactive. Please contact support.");
        }

        // Validate new password (add your password policy here)
        if (newPassword == null || newPassword.length() < 8) {
            throw new BadRequestException("Password must be at least 8 characters long");
        }

        // Reset password
        user.setFailedLoginAttempts(0);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordTemporary(false); // Clear temporary password flag
        user.setLastLoginAt(LocalDateTime.now()); // Update last login time
        //user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("First-time password reset successful for user: {}", username);
    }

    // Forgot password - generate and send reset token
    // Commenting the Old Logic
//    @Transactional
//    public void forgotPassword(String email) {
//        // Find user by email (username field contains email)
//        User user = userRepository.findByUsername(email).orElse(null);
//
//        // Always return success (security best practice - prevent email enumeration)
//        if (user == null) {
//            log.info("Password reset requested for non-existent email: {}", email);
//            return;
//        }
//
//        // Check if account is locked
//        if (user.isAccountLocked()) {
//            log.warn("Password reset requested for locked account: {}", email);
//            throw new BadRequestException("Account is locked. Please contact support.");
//        }
//
//        // Check if user is active
//        if (!user.isActive()) {
//            log.warn("Password reset requested for inactive account: {}", email);
//            throw new BadRequestException("Account is inactive. Please contact support.");
//        }
//
//        // Generate secure token
//        String token = generateSecureToken();
//        LocalDateTime expiry = LocalDateTime.now().plusHours(tokenExpirationHours);
//
//        // Save token to database
//        user.setResetPasswordToken(token);
//        user.setResetPasswordExpires(expiry);
//        userRepository.save(user);
//
//        // Send email with reset link
//        try {
//            passwordResetEmailService.sendPasswordResetEmail(user.getUsername(), token);
//            log.info("Password reset token generated and email sent for user: {}", email);
//        } catch (Exception e) {
//            log.error("Failed to send password reset email", e);
//            // Clear token if email fails
//            user.setResetPasswordToken(null);
//            user.setResetPasswordExpires(null);
//            userRepository.save(user);
//            throw new RuntimeException("Failed to send password reset email. Please try again.");
//        }
//    }

    @Transactional
    public void forgotPassword(String email) {

        User user = userRepository.findByUsername(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        if (user.isAccountLocked()) {
            throw new BadRequestException("Account is locked. Please contact support.");
        }

        if (!user.isActive()) {
            throw new BadRequestException("Account is inactive. Please contact support.");
        }

        String otp = generateOtp();

        user.setPasswordResetOtp(otp);
        user.setPasswordResetOtpExpires(
                LocalDateTime.now().plusMinutes(10)
        );

        userRepository.save(user);

        sendPasswordResetOtp(user.getUsername(), otp);

        log.info("Password reset OTP sent successfully to {}", email);
    }

    @Transactional
    public String verifyPasswordResetOtp(String email, String otp) {

        User user = userRepository.findByUsername(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        if (user.getPasswordResetOtp() == null ||
                user.getPasswordResetOtpExpires() == null) {
            throw new BadRequestException("No OTP found. Please request a new OTP.");
        }

        if (user.getPasswordResetOtpExpires().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("OTP has expired. Please request a new OTP.");
        }

        if (!user.getPasswordResetOtp().equals(otp)) {
            throw new BadRequestException("Invalid OTP.");
        }

        // Generate reset token
        String resetToken = generateSecureToken();

        user.setResetPasswordToken(resetToken);
        user.setResetPasswordExpires(
                LocalDateTime.now().plusMinutes(15));

        // Clear OTP after successful verification
        user.setPasswordResetOtp(null);
        user.setPasswordResetOtpExpires(null);

        userRepository.save(user);

        log.info("OTP verified successfully for {}", email);

        return resetToken;
    }

    // Validate reset token
    public boolean validateResetToken(String token) {
        LocalDateTime now = LocalDateTime.now();
        return userRepository.findByValidResetToken(token, now).isPresent();
    }

    // Reset password with token
    @Transactional
    public void resetPasswordWithToken(String token, String newPassword) {
        LocalDateTime now = LocalDateTime.now();
        User user = userRepository.findByValidResetToken(token, now)
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));

        // Check if account is locked
        if (user.isAccountLocked()) {
            throw new BadRequestException("Account is locked. Please contact support.");
        }

        // Check if user is active
        if (!user.isActive()) {
            throw new BadRequestException("Account is inactive. Please contact support.");
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(newPassword));

        // Clear reset token fields
        user.setPasswordResetOtp(null);
        user.setPasswordResetOtpExpires(null);
        user.setResetPasswordToken(null);
        user.setResetPasswordExpires(null);

        // Reset failed login attempts
        user.setFailedLoginAttempts(0);

        // Set password as not temporary
        user.setPasswordTemporary(false);

        userRepository.save(user);

        log.info("Password reset successfully with token for user: {}", user.getUsername());
    }

    // Helper method to generate secure token
    private String generateSecureToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    // Get user by username
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000); // always 6 digits
        return String.valueOf(otp);
    }

    public void sendPasswordResetOtp(String email, String otp) {

        String subject = "Password Reset OTP";

        String body = String.format("""
            Dear User,

            Your OTP for password reset is:

            %s

            This OTP is valid for 10 minutes.

            If you did not request this, please ignore this email.

            Regards,
            Pharma Team
            """, otp);

        emailService.sendMail(email, subject, body);
    }
}
