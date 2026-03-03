package com.example.pharmaaggregatorserver.service.seller.SellerLogIn;

// Rename this file to: PasswordResetEmailService.java
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetEmailService {  // Renamed from EmailService

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public void sendPasswordResetEmail(String to, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Password Reset Request - Pharma Aggregator");

            String resetLink = frontendUrl + "/reset-password?token=" + token;

            String emailContent = String.format(
                    "Hello,\n\n" +
                            "You have requested to reset your password for Pharma Aggregator.\n\n" +
                            "Please click the link below to reset your password:\n%s\n\n" +
                            "This link will expire in 1 hour.\n\n" +
                            "If you did not request this password reset, please ignore this email or contact support.\n\n" +
                            "Best regards,\n" +
                            "Pharma Aggregator Team",
                    resetLink
            );

            message.setText(emailContent);

            mailSender.send(message);
            log.info("Password reset email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", to, e);
            throw new RuntimeException("Failed to send password reset email. Please try again later.");
        }
    }
}
