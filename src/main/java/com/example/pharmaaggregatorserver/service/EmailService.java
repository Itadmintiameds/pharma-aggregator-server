package com.example.pharmaaggregatorserver.service;

import com.example.pharmaaggregatorserver.exception.ApplicationException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
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
    public void sendHtmlMailWithAttachment(
            String to, String subject, String htmlBody,
            byte[] attachmentBytes, String attachmentFilename) {

        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            // Attach PDF bytes directly — no temp file needed
            helper.addAttachment(
                    attachmentFilename,
                    new ByteArrayResource(attachmentBytes),
                    "application/pdf"
            );

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new ApplicationException("Failed to send approval email: " + e.getMessage());
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
