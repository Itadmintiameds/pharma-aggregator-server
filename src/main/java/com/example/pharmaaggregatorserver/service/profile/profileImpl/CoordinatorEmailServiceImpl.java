package com.example.pharmaaggregatorserver.service.profile.profileImpl;

import com.example.pharmaaggregatorserver.dto.seller.profile.CoordinatorEmailDTO;
import com.example.pharmaaggregatorserver.dto.seller.profile.SellerUpdateEmailDTO;
import com.example.pharmaaggregatorserver.service.profile.CoordinatorEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.ZonedDateTime;
import java.time.ZoneId;
//import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoordinatorEmailServiceImpl implements CoordinatorEmailService {

    private final JavaMailSender mailSender;

    @Value("${app.support.email:support@tiameds.com}")
    private String supportEmail;

    @Value("${app.login.url:https://marketplace.tiameds.com/login}")
    private String loginUrl;

    @Override
    public void sendHtmlMail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Override
    public void sendCoordinatorDetailsUpdateWithoutEmailChange(CoordinatorEmailDTO dto) {
        String subject = String.format("Your Coordinator Details Updated Successfully");

        String htmlBody = buildCoordinatorUpdateWithoutEmailChangeHtml(dto);

        sendHtmlMail(dto.getNewEmail(), subject, htmlBody);
        log.info("Coordinator details update email (without email change) sent to: {}", dto.getNewEmail());
    }

    @Override
    public void sendCoordinatorDetailsUpdateWithEmailChange(CoordinatorEmailDTO dto) {
        String subject = String.format("Your Coordinator Details Updated Successfully");

        String htmlBody = buildCoordinatorUpdateWithEmailChangeHtml(dto);

        sendHtmlMail(dto.getNewEmail(), subject, htmlBody);
        log.info("Coordinator details update email (with email change) sent to new email: {}", dto.getNewEmail());
    }

    @Override
    public void sendCoordinatorEmailChangeSecurityAlert(CoordinatorEmailDTO dto) {
        String subject = String.format("Security Alert – Your %s Company Coordinator Email Has Been Updated",
                dto.getSellerCompanyName());

        String htmlBody = buildEmailChangeSecurityAlertHtml(dto);

        sendHtmlMail(dto.getOldEmail(), subject, htmlBody);
        log.info("Security alert email sent to old email: {}", dto.getOldEmail());
    }

    @Override
    public void sendSellerUpdateSubmissionAcknowledgement(SellerUpdateEmailDTO dto) {
        String subject = String.format("Seller Profile Update Submission Acknowledgement - %s", dto.getRequestId());

        String htmlBody = buildSellerUpdateSubmissionAcknowledgementHtml(dto);

        sendHtmlMail(dto.getCoordinatorEmail(), subject, htmlBody);
        log.info("Seller update submission acknowledgement email sent to: {}", dto.getCoordinatorEmail());
    }

    @Override
    public void sendSellerUpdateApproved(SellerUpdateEmailDTO dto) {
        String subject = String.format("Updated Seller Profile - Approved - %s", dto.getSellerCompanyName());

        String htmlBody = buildSellerUpdateApprovedHtml(dto);

        sendHtmlMail(dto.getCoordinatorEmail(), subject, htmlBody);
        log.info("Seller update approved email sent to: {}", dto.getCoordinatorEmail());
    }

    @Override
    public void sendSellerUpdateRejected(SellerUpdateEmailDTO dto) {
        String subject = String.format("Updated Seller Profile - Rejected - %s", dto.getSellerCompanyName());

        String htmlBody = buildSellerUpdateRejectedHtml(dto);

        sendHtmlMail(dto.getCoordinatorEmail(), subject, htmlBody);
        log.info("Seller update rejected email sent to: {}", dto.getCoordinatorEmail());
    }

    @Override
    public void sendSellerProfileApproved(String coordinatorEmail, String coordinatorName, String sellerName,
                                          String sellerId, String approvedBy, String requestType) {
        String action = "CREATE".equals(requestType) ? "Created" : "Updated";
        String subject = String.format("Seller Profile %s Approved - %s", action, sellerName);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        String approvalTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).format(formatter);

        String htmlBody = String.format(
                "<!DOCTYPE html>" +
                        "<html>" +
                        "<head>" +
                        "<style>" +
                        "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                        ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                        ".header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }" +
                        ".content { background-color: #f9f9f9; padding: 20px; border: 1px solid #ddd; }" +
                        ".details { background-color: white; padding: 15px; margin: 15px 0; border-left: 4px solid #4CAF50; }" +
                        ".footer { background-color: #f1f1f1; padding: 15px; text-align: center; font-size: 12px; color: #666; border-radius: 0 0 5px 5px; }" +
                        "h2 { margin: 0; }" +
                        ".label { font-weight: bold; color: #555; }" +
                        ".value { margin-left: 10px; }" +
                        "</style>" +
                        "</head>" +
                        "<body>" +
                        "<div class='container'>" +
                        "<div class='header'>" +
                        "<h2>Seller Profile %s Approved</h2>" +
                        "</div>" +
                        "<div class='content'>" +
                        "<p>Dear <strong>%s</strong>,</p>" +
                        "<p>Good news! Your seller profile request has been <strong style='color: #4CAF50;'>APPROVED</strong>.</p>" +
                        "<div class='details'>" +
                        "<h3 style='margin-top: 0; color: #4CAF50;'>Seller Details</h3>" +
                        "<p><span class='label'>Seller ID:</span> <span class='value'>%s</span></p>" +
                        "<p><span class='label'>Seller Name:</span> <span class='value'>%s</span></p>" +
                        "<p><span class='label'>Request Type:</span> <span class='value'>%s</span></p>" +
                        "<p><span class='label'>Status:</span> <span class='value' style='color: #4CAF50; font-weight: bold;'>APPROVED</span></p>" +
                        "<p><span class='label'>Approved By:</span> <span class='value'>%s</span></p>" +
                        "<p><span class='label'>Approval Time:</span> <span class='value'>%s</span></p>" +
                        "</div>" +
                        "<p>The changes have been successfully applied to your account. You can now log in and continue using our services.</p>" +
                        "<p>If you have any questions, please contact our support team at <strong>%s</strong>.</p>" +
                        "</div>" +
                        "<div class='footer'>" +
                        "<p>Warm Regards,<br>TiaMeds Marketplace<br>Seller Onboarding & Compliance Team</p>" +
                        "<p>This is a system-generated email. Please do not reply.</p>" +
                        "</div>" +
                        "</div>" +
                        "</body>" +
                        "</html>",
                action,
                escapeHtml(coordinatorName != null ? coordinatorName : "Coordinator"),
                escapeHtml(sellerId != null ? sellerId : "N/A"),
                escapeHtml(sellerName),
                requestType,
                escapeHtml(approvedBy),
                approvalTime,
                escapeHtml(supportEmail)
        );

        sendHtmlMail(coordinatorEmail, subject, htmlBody);
        log.info("Seller profile approved email sent to: {}", coordinatorEmail);
    }

    @Override
    public void sendSellerProfileRejected(String coordinatorEmail, String coordinatorName, String sellerName,
                                          String rejectionReason, String rejectedBy, String requestType) {
        String action = "CREATE".equals(requestType) ? "Creation" : "Update";
        String subject = String.format("Seller Profile %s Rejected - %s", action, sellerName);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        String rejectionTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).format(formatter);

        String htmlBody = String.format(
                "<!DOCTYPE html>" +
                        "<html>" +
                        "<head>" +
                        "<style>" +
                        "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                        ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                        ".header { background-color: #f44336; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }" +
                        ".content { background-color: #f9f9f9; padding: 20px; border: 1px solid #ddd; }" +
                        ".details { background-color: white; padding: 15px; margin: 15px 0; border-left: 4px solid #f44336; }" +
                        ".reason-box { background-color: #fff3f3; padding: 15px; margin: 15px 0; border: 1px solid #ffcdd2; border-radius: 4px; }" +
                        ".footer { background-color: #f1f1f1; padding: 15px; text-align: center; font-size: 12px; color: #666; border-radius: 0 0 5px 5px; }" +
                        "h2 { margin: 0; }" +
                        ".label { font-weight: bold; color: #555; }" +
                        ".value { margin-left: 10px; }" +
                        ".rejection-title { color: #f44336; font-weight: bold; margin-top: 0; }" +
                        "</style>" +
                        "</head>" +
                        "<body>" +
                        "<div class='container'>" +
                        "<div class='header'>" +
                        "<h2>Seller Profile %s Rejected</h2>" +
                        "</div>" +
                        "<div class='content'>" +
                        "<p>Dear <strong>%s</strong>,</p>" +
                        "<p>We regret to inform you that your seller profile request has been <strong style='color: #f44336;'>REJECTED</strong>.</p>" +
                        "<div class='details'>" +
                        "<h3 style='margin-top: 0; color: #f44336;'>Rejection Details</h3>" +
                        "<p><span class='label'>Seller Name:</span> <span class='value'>%s</span></p>" +
                        "<p><span class='label'>Request Type:</span> <span class='value'>%s</span></p>" +
                        "<p><span class='label'>Status:</span> <span class='value' style='color: #f44336; font-weight: bold;'>REJECTED</span></p>" +
                        "<p><span class='label'>Rejected By:</span> <span class='value'>%s</span></p>" +
                        "<p><span class='label'>Rejection Time:</span> <span class='value'>%s</span></p>" +
                        "</div>" +
                        "<div class='reason-box'>" +
                        "<h4 class='rejection-title'>Rejection Reason:</h4>" +
                        "<p>%s</p>" +
                        "</div>" +
                        "<p>Please review the rejection reason, make the necessary corrections, and submit a new request.</p>" +
                        "<p>If you need clarification, please contact our support team at <strong>%s</strong>.</p>" +
                        "</div>" +
                        "<div class='footer'>" +
                        "<p>Warm Regards,<br>TiaMeds Marketplace<br>Seller Onboarding & Compliance Team</p>" +
                        "<p>This is a system-generated email. Please do not reply.</p>" +
                        "</div>" +
                        "</div>" +
                        "</body>" +
                        "</html>",
                action,
                escapeHtml(coordinatorName != null ? coordinatorName : "Coordinator"),
                escapeHtml(sellerName),
                requestType,
                escapeHtml(rejectedBy),
                rejectionTime,
                escapeHtml(rejectionReason != null ? rejectionReason : "No specific reason provided"),
                escapeHtml(supportEmail)
        );

        sendHtmlMail(coordinatorEmail, subject, htmlBody);
        log.info("Seller profile rejected email sent to: {}", coordinatorEmail);
    }

    @Override
    public void sendAutoApprovalEmail(String coordinatorEmail, String coordinatorName, String sellerId,
                                      String sellerName, String approvedBy) {
        String subject = String.format("Seller Profile Updated Successfully - %s", sellerName);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        String approvalTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).format(formatter);

        String htmlBody = String.format(
                "<!DOCTYPE html>" +
                        "<html>" +
                        "<head>" +
                        "<style>" +
                        "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                        ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                        ".header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }" +
                        ".content { background-color: #f9f9f9; padding: 20px; border: 1px solid #ddd; }" +
                        ".details { background-color: white; padding: 15px; margin: 15px 0; border-left: 4px solid #4CAF50; }" +
                        ".footer { background-color: #f1f1f1; padding: 15px; text-align: center; font-size: 12px; color: #666; border-radius: 0 0 5px 5px; }" +
                        "h2 { margin: 0; }" +
                        ".label { font-weight: bold; color: #555; }" +
                        ".value { margin-left: 10px; }" +
                        "</style>" +
                        "</head>" +
                        "<body>" +
                        "<div class='container'>" +
                        "<div class='header'>" +
                        "<h2>Seller Profile Updated Successfully</h2>" +
                        "</div>" +
                        "<div class='content'>" +
                        "<p>Dear <strong>%s</strong>,</p>" +
                        "<p>Your seller profile has been <strong style='color: #4CAF50;'>successfully updated</strong>.</p>" +
                        "<div class='details'>" +
                        "<h3 style='margin-top: 0; color: #4CAF50;'>Update Details</h3>" +
                        "<p><span class='label'>Seller ID:</span> <span class='value'>%s</span></p>" +
                        "<p><span class='label'>Seller Name:</span> <span class='value'>%s</span></p>" +
                        "<p><span class='label'>Status:</span> <span class='value' style='color: #4CAF50; font-weight: bold;'>UPDATED SUCCESSFULLY</span></p>" +
                        "<p><span class='label'>Updated By:</span> <span class='value'>%s</span></p>" +
                        "<p><span class='label'>Update Time:</span> <span class='value'>%s</span></p>" +
                        "</div>" +
                        "<p>The changes have been successfully applied to your account. No further action is required.</p>" +
                        "<p>If you have any questions, please contact our support team at <strong>%s</strong>.</p>" +
                        "</div>" +
                        "<div class='footer'>" +
                        "<p>Warm Regards,<br>TiaMeds Marketplace<br>Seller Onboarding & Compliance Team</p>" +
                        "<p>This is a system-generated email. Please do not reply.</p>" +
                        "</div>" +
                        "</div>" +
                        "</body>" +
                        "</html>",
                escapeHtml(coordinatorName),
                escapeHtml(sellerId),
                escapeHtml(sellerName),
                escapeHtml(approvedBy),
                approvalTime,
                escapeHtml(supportEmail)
        );

        sendHtmlMail(coordinatorEmail, subject, htmlBody);
        log.info("Auto-approval email sent to: {}", coordinatorEmail);
    }

    private String buildCoordinatorUpdateWithoutEmailChangeHtml(CoordinatorEmailDTO dto) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>")
                .append("<html>")
                .append("<head>")
                .append("<style>")
                .append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }")
                .append(".container { max-width: 600px; margin: 0 auto; padding: 20px; }")
                .append(".header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }")
                .append(".content { background-color: #f9f9f9; padding: 20px; border: 1px solid #ddd; }")
                .append(".details { background-color: white; padding: 15px; margin: 15px 0; border-left: 4px solid #4CAF50; }")
                .append(".footer { background-color: #f1f1f1; padding: 15px; text-align: center; font-size: 12px; color: #666; border-radius: 0 0 5px 5px; }")
                .append("h2 { margin: 0; }")
                .append(".label { font-weight: bold; color: #555; }")
                .append(".value { margin-left: 10px; }")
                .append("</style>")
                .append("</head>")
                .append("<body>")
                .append("<div class='container'>")
                .append("<div class='header'>")
                .append("<h2>Coordinator Details Updated Successfully</h2>")
                .append("</div>")
                .append("<div class='content'>")
                .append("<p>Dear <strong>").append(escapeHtml(dto.getCoordinatorName())).append("</strong>,</p>")
                .append("<p>Greetings from TiaMeds Marketplace.</p>")
                .append("<p>We would like to inform you that your <strong>").append(escapeHtml(dto.getSellerCompanyName()))
                .append("</strong> Company Coordinator details have been successfully updated.</p>")
                .append("<p>Below are the details that were updated:</p>")
                .append("<div class='details'>")
                .append("<h3 style='margin-top: 0; color: #4CAF50;'>Previous Details:</h3>")
                .append("<p><span class='label'>Coordinator Name:</span> <span class='value'>").append(escapeHtml(dto.getOldName())).append("</span></p>")
                .append("<p><span class='label'>Coordinator Designation:</span> <span class='value'>").append(escapeHtml(dto.getOldDesignation())).append("</span></p>")
                .append("<p><span class='label'>Coordinator Mobile Number:</span> <span class='value'>").append(escapeHtml(dto.getOldMobile())).append("</span></p>")
                .append("<p><span class='label'>Coordinator Email ID:</span> <span class='value'>").append(escapeHtml(dto.getOldEmail())).append("</span></p>")
                .append("</div>")
                .append("<div class='details'>")
                .append("<h3 style='margin-top: 0; color: #4CAF50;'>Updated Details:</h3>")
                .append("<p><span class='label'>Coordinator Name:</span> <span class='value'>").append(escapeHtml(dto.getNewName())).append("</span></p>")
                .append("<p><span class='label'>Coordinator Designation:</span> <span class='value'>").append(escapeHtml(dto.getNewDesignation())).append("</span></p>")
                .append("<p><span class='label'>Coordinator Mobile Number:</span> <span class='value'>").append(escapeHtml(dto.getNewMobile())).append("</span></p>")
                .append("<p><span class='label'>Coordinator Email ID:</span> <span class='value'>").append(escapeHtml(dto.getNewEmail())).append("</span></p>")
                .append("</div>")
                .append("<p>If you did not authorize this update or believe there is an error, kindly contact our support team at <strong>")
                .append(escapeHtml(dto.getSupportEmail() != null ? dto.getSupportEmail() : supportEmail)).append("</strong>.</p>")
                .append("</div>")
                .append("<div class='footer'>")
                .append("<p>Warm Regards,<br>TiaMeds Marketplace<br>Seller Onboarding & Compliance Team</p>")
                .append("<p>This is a system-generated email. Please do not reply.</p>")
                .append("</div>")
                .append("</div>")
                .append("</body>")
                .append("</html>");

        return html.toString();
    }

    private String buildCoordinatorUpdateWithEmailChangeHtml(CoordinatorEmailDTO dto) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>")
                .append("<html>")
                .append("<head>")
                .append("<style>")
                .append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }")
                .append(".container { max-width: 600px; margin: 0 auto; padding: 20px; }")
                .append(".header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }")
                .append(".content { background-color: #f9f9f9; padding: 20px; border: 1px solid #ddd; }")
                .append(".details { background-color: white; padding: 15px; margin: 15px 0; border-left: 4px solid #4CAF50; }")
                .append(".security-box { background-color: #fff3e0; padding: 15px; margin: 15px 0; border-left: 4px solid #ff9800; }")
                .append(".footer { background-color: #f1f1f1; padding: 15px; text-align: center; font-size: 12px; color: #666; border-radius: 0 0 5px 5px; }")
                .append("h2 { margin: 0; }")
                .append(".label { font-weight: bold; color: #555; }")
                .append(".value { margin-left: 10px; }")
                .append("</style>")
                .append("</head>")
                .append("<body>")
                .append("<div class='container'>")
                .append("<div class='header'>")
                .append("<h2>Coordinator Details Updated Successfully</h2>")
                .append("</div>")
                .append("<div class='content'>")
                .append("<p>Dear <strong>").append(escapeHtml(dto.getCoordinatorName())).append("</strong>,</p>")
                .append("<p>Greetings from TiaMeds Marketplace.</p>")
                .append("<p>We would like to inform you that your <strong>").append(escapeHtml(dto.getSellerCompanyName()))
                .append("</strong> Company Coordinator details have been successfully updated.</p>")
                .append("<p>Below are the details that were updated:</p>")
                .append("<div class='details'>")
                .append("<h3 style='margin-top: 0; color: #4CAF50;'>Previous Details:</h3>")
                .append("<p><span class='label'>Coordinator Name:</span> <span class='value'>").append(escapeHtml(dto.getOldName())).append("</span></p>")
                .append("<p><span class='label'>Coordinator Designation:</span> <span class='value'>").append(escapeHtml(dto.getOldDesignation())).append("</span></p>")
                .append("<p><span class='label'>Coordinator Mobile Number:</span> <span class='value'>").append(escapeHtml(dto.getOldMobile())).append("</span></p>")
                .append("<p><span class='label'>Coordinator Email ID:</span> <span class='value'>").append(escapeHtml(dto.getOldEmail())).append("</span></p>")
                .append("</div>")
                .append("<div class='details'>")
                .append("<h3 style='margin-top: 0; color: #4CAF50;'>Updated Details:</h3>")
                .append("<p><span class='label'>Coordinator Name:</span> <span class='value'>").append(escapeHtml(dto.getNewName())).append("</span></p>")
                .append("<p><span class='label'>Coordinator Designation:</span> <span class='value'>").append(escapeHtml(dto.getNewDesignation())).append("</span></p>")
                .append("<p><span class='label'>Coordinator Mobile Number:</span> <span class='value'>").append(escapeHtml(dto.getNewMobile())).append("</span></p>")
                .append("<p><span class='label'>Coordinator Email ID:</span> <span class='value'>").append(escapeHtml(dto.getNewEmail())).append("</span></p>")
                .append("</div>")
                .append("<div class='security-box'>")
                .append("<h3 style='margin-top: 0; color: #ff9800;'>Security Information</h3>")
                .append("<p>As part of TiaMeds Marketplace security protocol, your login credentials have been regenerated:</p>")
                .append("<p><span class='label'>New Username:</span> <span class='value'><strong>").append(escapeHtml(dto.getNewEmail())).append("</strong></span></p>")
                .append("<p><span class='label'>Temporary Password:</span> <span class='value'><strong>").append(escapeHtml(dto.getTemporaryPassword())).append("</strong></span></p>")
                .append("<p><span class='label'>Login Link:</span> <span class='value'><a href='").append(escapeHtml(dto.getLoginUrl() != null ? dto.getLoginUrl() : loginUrl)).append("'>")
                .append(escapeHtml(dto.getLoginUrl() != null ? dto.getLoginUrl() : loginUrl)).append("</a></span></p>")
                .append("<p><strong>For security reasons, you will be required to change your password upon first login.</strong></p>")
                .append("</div>")
                .append("<p>If you did not authorize this update or believe there is an error, kindly contact our support team at <strong>")
                .append(escapeHtml(dto.getSupportEmail() != null ? dto.getSupportEmail() : supportEmail)).append("</strong>.</p>")
                .append("</div>")
                .append("<div class='footer'>")
                .append("<p>Warm Regards,<br>TiaMeds Marketplace<br>Seller Onboarding & Compliance Team</p>")
                .append("<p>This is a system-generated email. Please do not reply.</p>")
                .append("</div>")
                .append("</div>")
                .append("</body>")
                .append("</html>");

        return html.toString();
    }

    private String buildEmailChangeSecurityAlertHtml(CoordinatorEmailDTO dto) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>")
                .append("<html>")
                .append("<head>")
                .append("<style>")
                .append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }")
                .append(".container { max-width: 600px; margin: 0 auto; padding: 20px; }")
                .append(".header { background-color: #f44336; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }")
                .append(".content { background-color: #f9f9f9; padding: 20px; border: 1px solid #ddd; }")
                .append(".alert-box { background-color: #ffebee; padding: 15px; margin: 15px 0; border-left: 4px solid #f44336; }")
                .append(".footer { background-color: #f1f1f1; padding: 15px; text-align: center; font-size: 12px; color: #666; border-radius: 0 0 5px 5px; }")
                .append("h2 { margin: 0; }")
                .append(".label { font-weight: bold; color: #555; }")
                .append(".value { margin-left: 10px; }")
                .append("</style>")
                .append("</head>")
                .append("<body>")
                .append("<div class='container'>")
                .append("<div class='header'>")
                .append("<h2>⚠ Security Alert – Coordinator Email Changed</h2>")
                .append("</div>")
                .append("<div class='content'>")
                .append("<p>Dear <strong>").append(escapeHtml(dto.getCoordinatorName())).append("</strong>,</p>")
                .append("<p>This is a security notification from TiaMeds Marketplace.</p>")
                .append("<p>We would like to inform you that the coordinator email associated with your seller profile for <strong>")
                .append(escapeHtml(dto.getSellerCompanyName())).append("</strong> has been updated.</p>")
                .append("<div class='alert-box'>")
                .append("<h3 style='margin-top: 0; color: #f44336;'>Details of the Change:</h3>")
                .append("<p><span class='label'>Previous Email ID:</span> <span class='value'><strong>").append(escapeHtml(dto.getOldEmail())).append("</strong></span></p>")
                .append("<p><span class='label'>Updated Email ID:</span> <span class='value'><strong>").append(escapeHtml(dto.getNewEmail())).append("</strong></span></p>")
                .append("</div>")
                .append("<p>If you did not authorize this update or believe there is an error, kindly contact our support team at <strong>")
                .append(escapeHtml(dto.getSupportEmail() != null ? dto.getSupportEmail() : supportEmail)).append("</strong>.</p>")
                .append("</div>")
                .append("<div class='footer'>")
                .append("<p>Warm Regards,<br>TiaMeds Marketplace<br>Security & Compliance Team</p>")
                .append("<p>This is a system-generated email. Please do not reply.</p>")
                .append("</div>")
                .append("</div>")
                .append("</body>")
                .append("</html>");

        return html.toString();
    }

    private String buildSellerUpdateSubmissionAcknowledgementHtml(SellerUpdateEmailDTO dto) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>")
                .append("<html>")
                .append("<head>")
                .append("<style>")
                .append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }")
                .append(".container { max-width: 600px; margin: 0 auto; padding: 20px; }")
                .append(".header { background-color: #2196F3; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }")
                .append(".content { background-color: #f9f9f9; padding: 20px; border: 1px solid #ddd; }")
                .append(".details { background-color: white; padding: 15px; margin: 15px 0; border-left: 4px solid #2196F3; }")
                .append(".footer { background-color: #f1f1f1; padding: 15px; text-align: center; font-size: 12px; color: #666; border-radius: 0 0 5px 5px; }")
                .append("h2 { margin: 0; }")
                .append(".label { font-weight: bold; color: #555; }")
                .append(".value { margin-left: 10px; }")
                .append("</style>")
                .append("</head>")
                .append("<body>")
                .append("<div class='container'>")
                .append("<div class='header'>")
                .append("<h2>Seller Profile Update Submission Acknowledgement</h2>")
                .append("</div>")
                .append("<div class='content'>")
                .append("<p>Dear <strong>").append(escapeHtml(dto.getCoordinatorName())).append("</strong>,</p>")
                .append("<p>Greetings from TiaMeds Marketplace.</p>")
                .append("<p>We would like to inform you that your <strong>").append(escapeHtml(dto.getSellerCompanyName()))
                .append("</strong> profile update request has been successfully received, and the Updated Request ID is <strong>")
                .append(escapeHtml(dto.getRequestId())).append("</strong>.</p>")
                .append("<p>As per the TiaMeds Marketplace platform compliance, the updated details must undergo verification by TiaMeds Admin.</p>")
                .append("<p>Once the review is completed, you will be notified via Email & SMS.</p>")
                .append("<div class='details'>")
                .append("<h3 style='margin-top: 0; color: #2196F3;'>Request Details:</h3>")
                .append("<p><span class='label'>Updated Request ID:</span> <span class='value'>").append(escapeHtml(dto.getRequestId())).append("</span></p>")
                .append("</div>")
                .append("<p>Kindly retain the above Updated Request ID for future reference.</p>")
                .append("<p>For any assistance, please contact our support team at <strong>")
                .append(escapeHtml(dto.getSupportEmail() != null ? dto.getSupportEmail() : supportEmail)).append("</strong>.</p>")
                .append("</div>")
                .append("<div class='footer'>")
                .append("<p>Warm regards,<br>TiaMeds Marketplace</p>")
                .append("<p>This is a system-generated email. Please do not reply to this message.</p>")
                .append("</div>")
                .append("</div>")
                .append("</body>")
                .append("</html>");

        return html.toString();
    }

    private String buildSellerUpdateApprovedHtml(SellerUpdateEmailDTO dto) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>")
                .append("<html>")
                .append("<head>")
                .append("<style>")
                .append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }")
                .append(".container { max-width: 600px; margin: 0 auto; padding: 20px; }")
                .append(".header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }")
                .append(".content { background-color: #f9f9f9; padding: 20px; border: 1px solid #ddd; }")
                .append(".details { background-color: white; padding: 15px; margin: 15px 0; border-left: 4px solid #4CAF50; }")
                .append(".footer { background-color: #f1f1f1; padding: 15px; text-align: center; font-size: 12px; color: #666; border-radius: 0 0 5px 5px; }")
                .append("h2 { margin: 0; }")
                .append(".label { font-weight: bold; color: #555; }")
                .append(".value { margin-left: 10px; }")
                .append("</style>")
                .append("</head>")
                .append("<body>")
                .append("<div class='container'>")
                .append("<div class='header'>")
                .append("<h2>Updated Seller Profile - Approved</h2>")
                .append("</div>")
                .append("<div class='content'>")
                .append("<p>Dear <strong>").append(escapeHtml(dto.getCoordinatorName())).append("</strong>,</p>")
                .append("<p>We are pleased to inform you that the updated profile details for <strong>").append(escapeHtml(dto.getSellerCompanyName()))
                .append("</strong> have been successfully reviewed and approved by our compliance team.</p>")
                .append("<p>Your seller profile has now been updated as per the requested changes.</p>")
                .append("<p>Below are your update request details:</p>")
                .append("<div class='details'>")
                .append("<h3 style='margin-top: 0; color: #4CAF50;'>Update Details:</h3>")
                .append("<p><span class='label'>Updated Request ID:</span> <span class='value'>").append(escapeHtml(dto.getRequestId())).append("</span></p>")
                .append("<p><span class='label'>Seller ID:</span> <span class='value'>").append(escapeHtml(dto.getSellerId())).append("</span></p>")
                .append("<p><span class='label'>Seller Company Name:</span> <span class='value'>").append(escapeHtml(dto.getSellerCompanyName())).append("</span></p>");

        if (dto.getAdminComments() != null && !dto.getAdminComments().isEmpty()) {
            html.append("<p><span class='label'>Admin Comments:</span> <span class='value'>").append(escapeHtml(dto.getAdminComments())).append("</span></p>");
        }

        html.append("</div>")
                .append("<p>Please note that your Seller ID <strong>").append(escapeHtml(dto.getSellerId()))
                .append("</strong> remains your company's unique identification number on the TiaMeds Marketplace platform and will continue to be used for all future transactions and correspondence.</p>")
                .append("<p>If you have any questions or require assistance, please feel free to contact our support team at <strong>")
                .append(escapeHtml(dto.getSupportEmail() != null ? dto.getSupportEmail() : supportEmail)).append("</strong>.</p>")
                .append("</div>")
                .append("<div class='footer'>")
                .append("<p>Warm regards,<br>TiaMeds Marketplace<br>Seller Onboarding & Compliance Team</p>")
                .append("<p>This is a system-generated email. Please do not reply.</p>")
                .append("</div>")
                .append("</div>")
                .append("</body>")
                .append("</html>");

        return html.toString();
    }

    private String buildSellerUpdateRejectedHtml(SellerUpdateEmailDTO dto) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>")
                .append("<html>")
                .append("<head>")
                .append("<style>")
                .append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }")
                .append(".container { max-width: 600px; margin: 0 auto; padding: 20px; }")
                .append(".header { background-color: #f44336; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }")
                .append(".content { background-color: #f9f9f9; padding: 20px; border: 1px solid #ddd; }")
                .append(".details { background-color: white; padding: 15px; margin: 15px 0; border-left: 4px solid #f44336; }")
                .append(".reason-box { background-color: #fff3f3; padding: 15px; margin: 15px 0; border: 1px solid #ffcdd2; border-radius: 4px; }")
                .append(".footer { background-color: #f1f1f1; padding: 15px; text-align: center; font-size: 12px; color: #666; border-radius: 0 0 5px 5px; }")
                .append("h2 { margin: 0; }")
                .append(".label { font-weight: bold; color: #555; }")
                .append(".value { margin-left: 10px; }")
                .append(".rejection-title { color: #f44336; font-weight: bold; margin-top: 0; }")
                .append("</style>")
                .append("</head>")
                .append("<body>")
                .append("<div class='container'>")
                .append("<div class='header'>")
                .append("<h2>Updated Seller Profile - Rejected</h2>")
                .append("</div>")
                .append("<div class='content'>")
                .append("<p>Dear <strong>").append(escapeHtml(dto.getCoordinatorName())).append("</strong>,</p>")
                .append("<p>Thank you for submitting the updated profile details for <strong>").append(escapeHtml(dto.getSellerCompanyName()))
                .append("</strong>, and the Updated Request ID is <strong>").append(escapeHtml(dto.getRequestId())).append("</strong>.</p>")
                .append("<p>After a detailed review of the information and documents you submitted, we regret to inform you that your seller profile update request has been rejected due to the following reasons:</p>")
                .append("<div class='reason-box'>")
                .append("<h4 class='rejection-title'>Rejection Reason(s):</h4>")
                .append("<p>").append(escapeHtml(dto.getRejectionReason())).append("</p>")
                .append("</div>")
                .append("<p>As a compliance-first pharmaceutical marketplace, TiaMeds is required to ensure that all seller profiles and any updated details fully meet statutory, regulatory, and platform policy requirements.</p>")
                .append("<p>You may resubmit the updated details after addressing the above-mentioned issues and ensuring that all required information and documents are accurate and complete.</p>")
                .append("<p>For any clarification, please contact our support team at <strong>")
                .append(escapeHtml(dto.getSupportEmail() != null ? dto.getSupportEmail() : supportEmail)).append("</strong>.</p>")
                .append("<p>Thank you for your continued association with the TiaMeds Marketplace platform.</p>")
                .append("</div>")
                .append("<div class='footer'>")
                .append("<p>Warm regards,<br>TiaMeds Marketplace<br>Seller Onboarding & Compliance Team</p>")
                .append("<p>This is a system-generated email. Please do not reply.</p>")
                .append("</div>")
                .append("</div>")
                .append("</body>")
                .append("</html>");

        return html.toString();
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}