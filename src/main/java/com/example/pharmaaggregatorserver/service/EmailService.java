package com.example.pharmaaggregatorserver.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // 🔹 Simple text mail
    public void sendMail(String to, String subject, String body) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    // 🔹 Approval mail with PDF attachment
    public void sendApprovalMail(String to,
                                 String username,
                                 String password,
                                 String resetLink,
                                 String pdfPath) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Seller Account Approved 🎉");

            helper.setText("""
                    Dear Seller,
                    
                    Your seller account has been approved.
                    
                    Username: %s
                    Password: %s
                    Set your password here: %s
                    
                    Please find your agreement attached.
                    
                    Regards,
                    Pharma Aggregator Team
                    """.formatted(username, password, resetLink), false); // false = plain text

            FileSystemResource file = new FileSystemResource(new File(pdfPath));

            if (file.exists()) {
                helper.addAttachment("Seller_Agreement.pdf", file);
            } else {
                throw new RuntimeException("PDF file not found at " + pdfPath);
            }

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Approval email sending failed", e);
        }
    }

    public void sendHtmlMail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // TRUE = HTML

            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send HTML email", e);
        }
    }

    public void sendCoordinatorOtp(String to, String otp) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Pharma Aggregator - Email Verification OTP");
        message.setText(
                "Dear Coordinator,\n\n" +
                        "Your email verification OTP is: " + otp + "\n\n" +
                        "This OTP is valid for 5 minutes.\n" +
                        "Please do not share this code with anyone.\n\n" +
                        "Regards,\nPharma Aggregator Team"
        );

        mailSender.send(message);
    }

}
