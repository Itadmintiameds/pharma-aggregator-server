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
        content.append("Your application has been successfully submitted to Pharma Aggregator.\n\n");

        content.append("═══════════════════════════════════════════\n");
        content.append("         APPLICATION DETAILS              \n");
        content.append("═══════════════════════════════════════════\n\n");

        content.append("📌 Application Request ID: ").append(request.getApplicationRequestId()).append("\n");
        content.append("📅 Submission Date: ").append(submissionDate).append("\n");
        content.append("✅ Status: SUBMITTED\n\n");

        content.append("───────────────────────────────────────────\n");
        content.append("          SELLER INFORMATION              \n");
        content.append("───────────────────────────────────────────\n\n");
        content.append("🏢 Seller Name: ").append(request.getSellerName() != null ? request.getSellerName() : "Not Provided").append("\n");
        content.append("📧 Email: ").append(request.getSellerEmail() != null ? request.getSellerEmail() : "Not Provided").append("\n");
        content.append("📞 Phone: ").append(request.getSellerPhone() != null ? request.getSellerPhone() : "Not Provided").append("\n\n");

        content.append("───────────────────────────────────────────\n");
        content.append("        COORDINATOR INFORMATION           \n");
        content.append("───────────────────────────────────────────\n\n");
        content.append("👤 Name: ").append(request.getCoordinatorName()).append("\n");
        content.append("📧 Email: ").append(request.getCoordinatorEmail()).append("\n");
        content.append("📞 Mobile: ").append(request.getCoordinatorMobile() != null ? request.getCoordinatorMobile() : "Not Provided").append("\n");
        content.append("💼 Designation: ").append(request.getCoordinatorDesignation() != null ? request.getCoordinatorDesignation() : "Coordinator").append("\n\n");

        content.append("───────────────────────────────────────────\n");
        content.append("              NEXT STEPS                  \n");
        content.append("───────────────────────────────────────────\n\n");
        content.append("1️⃣ Our team will review your application\n");
        content.append("2️⃣ Verification process will be initiated\n");
        content.append("3️⃣ You will receive another email once approved\n\n");

        content.append("⚠️ Please quote your Application Request ID: **").append(request.getApplicationRequestId())
                .append("** in all future correspondence.\n\n");

        content.append("───────────────────────────────────────────\n");
        content.append("Thank you for registering with Pharma Aggregator!\n");
        content.append("───────────────────────────────────────────\n\n");

        content.append("Best Regards,\n");
        content.append("Pharma Aggregator Team\n");
        content.append("---\n");
        content.append("This is an automated email, please do not reply.\n");

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
