package com.example.pharmaaggregatorserver.service.serviceImpl.temp.seller.OnSubmit;


import com.example.pharmaaggregatorserver.dto.seller.OnSubmit.EmailRequestDTO;
import com.example.pharmaaggregatorserver.dto.seller.OnSubmit.EmailResponseDTO;
import com.example.pharmaaggregatorserver.dto.seller.OnSubmit.EmailStatusDTO;

import com.example.pharmaaggregatorserver.service.temp.seller.OnSubmit.IndependentEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class IndependentEmailServiceImpl implements IndependentEmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // In-memory storage for email status (replace with database in production)
    private final ConcurrentHashMap<String, EmailStatusDTO> emailStatusStore = new ConcurrentHashMap<>();

    @Override
    public EmailResponseDTO sendApplicationConfirmationEmail(EmailRequestDTO request) {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String formattedDate = today.format(formatter);

        String subject = "Application Submitted Successfully - Pharma Aggregator Registration";
        String content = buildEmailContent(request, formattedDate);

        boolean emailSent = sendEmail(request.getCoordinatorEmail(), subject, content);

        // Store status
        EmailStatusDTO status = new EmailStatusDTO();
        status.setApplicationRequestId(request.getApplicationRequestId());
        status.setCoordinatorEmail(request.getCoordinatorEmail());
        status.setEmailSent(emailSent);
        status.setEmailSentAt(emailSent ? LocalDateTime.now() : null);
        status.setEmailStatus(emailSent ? "SENT" : "FAILED");
        status.setErrorMessage(emailSent ? null : "Failed to send email");
        emailStatusStore.put(request.getApplicationRequestId(), status);

        // Prepare response
        EmailResponseDTO response = new EmailResponseDTO();
        response.setSuccess(emailSent);
        response.setMessage(emailSent ? "Confirmation email sent successfully" : "Failed to send confirmation email");
        response.setApplicationRequestId(request.getApplicationRequestId());
        response.setSubmissionDate(formattedDate);
        response.setCoordinatorEmail(request.getCoordinatorEmail());
        response.setCoordinatorName(request.getCoordinatorName());
        response.setTimestamp(LocalDateTime.now());

        return response;
    }

    @Override
    public EmailResponseDTO sendCustomEmail(String to, String subject, String body) {
        boolean emailSent = sendEmail(to, subject, body);

        EmailResponseDTO response = new EmailResponseDTO();
        response.setSuccess(emailSent);
        response.setMessage(emailSent ? "Custom email sent successfully" : "Failed to send custom email");
        response.setCoordinatorEmail(to);
        response.setTimestamp(LocalDateTime.now());
        response.setSubmissionDate(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        return response;
    }

    @Override
    public EmailStatusDTO checkEmailStatus(String applicationRequestId) {
        return emailStatusStore.getOrDefault(applicationRequestId,
                new EmailStatusDTO(applicationRequestId, null, false, null, "NOT_FOUND", "No email record found"));
    }

    @Async
    public boolean sendEmail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);

            mailSender.send(message);
            log.info("✅ Email sent successfully to: {}", to);
            return true;
        } catch (Exception e) {
            log.error("❌ Failed to send email to: {} - Error: {}", to, e.getMessage());
            return false;
        }
    }

    private String buildEmailContent(EmailRequestDTO request, String submissionDate) {
        StringBuilder content = new StringBuilder();

        content.append("Dear ").append(request.getCoordinatorName()).append(",\n\n");

        content.append("Greetings from TiaMeds Marketplace.\n\n");

        content.append("Thank you for your interest in onboarding ").append(request.getSellerName() != null ? request.getSellerName() : "your company")
                .append(" as a Seller Company on the TiaMeds Marketplace platform and for submitting the registration details.\n\n");

        content.append("We are pleased to inform you that the registration details of ")
                .append(request.getSellerName() != null ? request.getSellerName() : "your company")
                .append(" have been received successfully, and the Request ID is ").append(request.getApplicationRequestId()).append(".\n\n");

        content.append("Your application is currently under review by the TiaMeds Admin Team. Once the review process is completed, you will be notified via Email & SMS.\n\n");

        content.append("Kindly retain the above Request ID for future reference.\n\n");

        content.append("For any assistance, please contact our support team at [Support Email ID].\n\n");

        content.append("We appreciate your cooperation during this process.\n\n");

        content.append("Warm regards,\n");
        content.append("TiaMeds Marketplace\n");
        content.append("This is a system-generated email. Please do not reply to this message.\n");

        return content.toString();
    }

    // Helper method to mask account number
    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) {
            return "XXXX";
        }
        return accountNumber.substring(accountNumber.length() - 4);
    }
}
