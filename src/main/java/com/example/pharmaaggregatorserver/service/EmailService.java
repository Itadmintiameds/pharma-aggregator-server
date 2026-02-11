package com.example.pharmaaggregatorserver.service;

import com.example.pharmaaggregatorserver.exception.ApplicationException;
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
    public void sendHtmlMailWithAttachment(String to,
                                           String subject,
                                           String htmlBody,
                                           String filePath,
                                           String attachmentName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();

            // TRUE = multipart email (required for attachments)
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // HTML

            FileSystemResource file = new FileSystemResource(new File(filePath));

            if (file.exists()) {
                helper.addAttachment(attachmentName, file);
            } else {
                throw new ApplicationException("Attachment file not found at " + filePath);
            }

            mailSender.send(message);

        } catch (Exception e) {
            e.printStackTrace();
            throw new ApplicationException("Failed to send HTML email with attachment");
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

}
