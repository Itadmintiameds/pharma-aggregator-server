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
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public record EmailAttachment(String filename, byte[] bytes, String contentType) {
    }

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

    // Same as sendHtmlMailWithAttachment but supports zero-or-more attachments
    // in one email (e.g. one invoice PDF per seller on a multi-seller order),
    // instead of exactly one.
    public void sendHtmlMailWithAttachments(
            String to, String subject, String htmlBody, List<EmailAttachment> attachments) {

        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            if (attachments != null) {
                for (EmailAttachment attachment : attachments) {
                    helper.addAttachment(
                            attachment.filename(),
                            new ByteArrayResource(attachment.bytes()),
                            attachment.contentType());
                }
            }

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new ApplicationException("Failed to send email with attachments: " + e.getMessage());
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

        String subject = "Your TiaMeds Marketplace Verification Code";
        String body = buildOtpEmailTemplate(otp);

        sendHtmlMail(to, subject, body);
    }

    public void sendBuyerOtp(String to, String otp) {

        String subject = "Your TiaMeds Marketplace Verification Code";
        String body = buildBuyerOtpEmailTemplate(otp);

        sendHtmlMail(to, subject, body);
    }

    /**
     * Builds a branded, responsive HTML template for OTP verification emails.
     */
    private String buildOtpEmailTemplate(String otp) {
        return """
                <!DOCTYPE html>
                <html>
                <body style="margin:0; padding:0; background-color:#f4f6f8; font-family:Arial, Helvetica, sans-serif;">
                  <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f4f6f8; padding:32px 0;">
                    <tr>
                      <td align="center">
                        <table role="presentation" width="480" cellpadding="0" cellspacing="0" style="background-color:#ffffff; border-radius:8px; overflow:hidden; box-shadow:0 1px 3px rgba(0,0,0,0.08);">
                          <tr>
                            <td style="background-color:#9659FD; padding:24px 32px;">
                              <span style="color:#ffffff; font-size:20px; font-weight:bold; letter-spacing:0.3px;">TiaMeds Marketplace</span>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:32px;">
                              <p style="margin:0 0 16px; font-size:15px; color:#333333; line-height:1.6;">
                                Dear Coordinator,
                              </p>
                              <p style="margin:0 0 24px; font-size:15px; color:#333333; line-height:1.6;">
                                Use the verification code below to confirm your email address. This code is valid for
                                <b>5 minutes</b>.
                              </p>
                              <table role="presentation" width="100%%" cellpadding="0" cellspacing="0">
                                <tr>
                                  <td align="center" style="padding:16px 0 24px;">
                                    <span style="display:inline-block; -webkit-user-select:all; user-select:all; background-color:#f2e9ff; color:#9659FD; font-size:32px; font-weight:bold; letter-spacing:8px; padding:14px 28px; border-radius:6px; font-family:'Courier New', monospace;">
                                      %s
                                    </span>
                                  </td>
                                </tr>
                              </table>
                              <p style="margin:0 0 8px; font-size:14px; color:#666666; line-height:1.6;">
                                For your security, please do not share this code with anyone, including TiaMeds staff.
                              </p>
                              <p style="margin:0; font-size:14px; color:#666666; line-height:1.6;">
                                If you did not request this code, you can safely ignore this email.
                              </p>
                            </td>
                          </tr>
                          <tr>
                            <td style="background-color:#f4f6f8; padding:20px 32px; border-top:1px solid #e5e8eb;">
                              <p style="margin:0; font-size:12px; color:#999999; line-height:1.6;">
                                Warm Regards,<br>
                                TiaMeds Marketplace<br>
                                Seller Onboarding &amp; Compliance Team
                              </p>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(otp);
    }

    /**
     * Buyer-facing variant of {@link #buildOtpEmailTemplate(String)} — same layout,
     * greeting/footer wording addressed to a buyer instead of a seller coordinator.
     */
    private String buildBuyerOtpEmailTemplate(String otp) {
        return """
                <!DOCTYPE html>
                <html>
                <body style="margin:0; padding:0; background-color:#f4f6f8; font-family:Arial, Helvetica, sans-serif;">
                  <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f4f6f8; padding:32px 0;">
                    <tr>
                      <td align="center">
                        <table role="presentation" width="480" cellpadding="0" cellspacing="0" style="background-color:#ffffff; border-radius:8px; overflow:hidden; box-shadow:0 1px 3px rgba(0,0,0,0.08);">
                          <tr>
                            <td style="background-color:#9659FD; padding:24px 32px;">
                              <span style="color:#ffffff; font-size:20px; font-weight:bold; letter-spacing:0.3px;">TiaMeds Marketplace</span>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:32px;">
                              <p style="margin:0 0 16px; font-size:15px; color:#333333; line-height:1.6;">
                                Hello,
                              </p>
                              <p style="margin:0 0 24px; font-size:15px; color:#333333; line-height:1.6;">
                                Use the verification code below to confirm your email address. This code is valid for
                                <b>5 minutes</b>.
                              </p>
                              <table role="presentation" width="100%%" cellpadding="0" cellspacing="0">
                                <tr>
                                  <td align="center" style="padding:16px 0 24px;">
                                    <span style="display:inline-block; -webkit-user-select:all; user-select:all; background-color:#f2e9ff; color:#9659FD; font-size:32px; font-weight:bold; letter-spacing:8px; padding:14px 28px; border-radius:6px; font-family:'Courier New', monospace;">
                                      %s
                                    </span>
                                  </td>
                                </tr>
                              </table>
                              <p style="margin:0 0 8px; font-size:14px; color:#666666; line-height:1.6;">
                                For your security, please do not share this code with anyone, including TiaMeds staff.
                              </p>
                              <p style="margin:0; font-size:14px; color:#666666; line-height:1.6;">
                                If you did not request this code, you can safely ignore this email.
                              </p>
                            </td>
                          </tr>
                          <tr>
                            <td style="background-color:#f4f6f8; padding:20px 32px; border-top:1px solid #e5e8eb;">
                              <p style="margin:0; font-size:12px; color:#999999; line-height:1.6;">
                                Warm Regards,<br>
                                TiaMeds Marketplace<br>
                                Buyer Support Team
                              </p>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(otp);
    }

}
