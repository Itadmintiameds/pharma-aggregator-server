package com.example.pharmaaggregatorserver.service.serviceImpl.admin;

import com.example.pharmaaggregatorserver.dto.seller.SellerApprovalRequestDTO;
import com.example.pharmaaggregatorserver.entity.temp.seller.TempSeller;
import com.example.pharmaaggregatorserver.exception.ApplicationException;
import com.example.pharmaaggregatorserver.exception.NotFoundException;
import com.example.pharmaaggregatorserver.repository.temp.seller.TempSellerRepository;
import com.example.pharmaaggregatorserver.service.EmailService;
import com.example.pharmaaggregatorserver.service.PdfService;
import com.example.pharmaaggregatorserver.service.admin.SellerApprovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerApprovalServiceImpl implements SellerApprovalService {

    public static final String SUPPORT_TIAMEDS_COM = "support@tiameds.com";
    public static final String LOGIN_URL = "https://testdomain.com/seller/login";
    private final TempSellerRepository tempSellerRepo;
    //    private final SellerRepository sellerRepo;
    private final EmailService emailService;
    private final PdfService pdfService;
//    private final UserService userService;

    /**
     * Processes admin review decision based on request status.
     * Possible actions:
     * - ACCEPT     → Approve seller and send agreement & credentials
     * - REJECT     → Reject seller and notify with reason
     * - CORRECTION → Request seller to update details
     *
     * @param request contains seller ID, status, and reviewer comments
     */
    @Override
    public void processReview(SellerApprovalRequestDTO request) {

        TempSeller tempSeller = tempSellerRepo.findById(request.getId())
                .orElseThrow(() -> new NotFoundException("Seller not found"));

        switch (request.getStatus().toUpperCase()) {

            case "CORRECTION" -> handleCorrection(tempSeller, request.getComments());

            case "REJECT" -> handleRejection(tempSeller, request.getComments());

            case "ACCEPT" -> handleApprovalForTempSeller(tempSeller, request.getComments());

//            case "ACCEPT" -> handleApproval(tempSeller);

            default -> throw new ApplicationException("Invalid Status");
        }
    }

    /**
     * Handles seller correction request.
     * Updates status and sends email with correction link.
     */
    private void handleCorrection(TempSeller seller, String comments) {

        // Update seller status
        seller.setStatus("CORRECTION_REQUIRED");
        tempSellerRepo.save(seller);

        // Correction URL
        String correctionUrl = "https://testdomain.com/seller/correction/" + seller.getTempSellerId();

        // HTML Email Body
        String body = """
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                
                    <p>Dear %s,</p>
                
                    <p>
                        Thank you for submitting your application to onboard as a seller company on the 
                        <b>TiaMeds Marketplace platform</b> and Your Application ID is <b>%s</b>.
                    </p>
                
                    <p>
                        Our compliance team has reviewed your application and identified certain items that 
                        require <b>correction or additional information</b> before we can proceed with approval.
                    </p>
                
                    <p><b>Please review and address the following points:</b><br>
                    %s</p>
                
                    <p>
                        Kindly log in to your application using the link below and update the required information:
                    </p>
                
                    <p>
                        Update Application Link: <a href="%s" style="color: #1a73e8; text-decoration: none;">
                            %s
                        </a>
                    </p>
                
                    <p>
                        Once the corrections are submitted, your application will be re-evaluated by our compliance team.
                    </p>
                
                    <p>
                        Please note that timely completion of corrections will help us process your application faster.
                    </p>
                
                    <p>
                        For any assistance, please contact our support team at 
                        <a href="mailto:%s">%s</a>.
                    </p>
                
                    <p>
                        Regards,<br>
                        TiaMeds Marketplace<br>
                        Seller Onboarding & Compliance Team
                    </p>
                
                </body>
                </html>
                """.formatted(
                seller.getSellerName(),
                seller.getTempSellerRequestId(),
                comments,
                correctionUrl,
                correctionUrl,
                SUPPORT_TIAMEDS_COM,
                SUPPORT_TIAMEDS_COM
        );

        // IMPORTANT: Use HTML email method
        emailService.sendHtmlMail(
                seller.getEmail(),
                "Action Required: Seller Application Correction",
                body
        );
    }

    /**
     * Handles seller rejection process.
     * Marks seller as rejected and sends rejection email.
     */
    private void handleRejection(TempSeller seller, String comments) {

        seller.setStatus("REJECTED");
        tempSellerRepo.save(seller);

        String body = """
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                
                <p>Dear %s,</p>
                
                <p>
                    Thank you for submitting your application to onboard as a seller company on the 
                    <b>TiaMeds Marketplace platform</b> and Your Application ID is <b>%s</b>.
                </p>
                
                <p>
                    After a detailed review of your submitted information and documents, we regret to inform 
                    you that your <b>%s</b> registration application has been rejected due to the following reasons:
                </p>
                
                <p><b>%s</b></p>
                
                <p>
                    As a compliance-first pharmaceutical marketplace, TiaMeds is required to ensure that all 
                    onboarded seller companies fully meet statutory, regulatory, and 
                    TiaMeds Marketplace platform policy requirements.
                </p>
                
                <p>
                    You may submit a fresh application after addressing the above-mentioned issues and 
                    ensuring that all required information and documents are accurate and complete.
                </p>
                
                <p>
                    For any clarification, please contact our support team at 
                    <a href="mailto:%s">%s</a>.
                </p>
                
                <p>
                    Thank you for showing your interest in the TiaMeds Marketplace platform.
                </p>
                
                <p>
                    Regards,<br>
                    TiaMeds Marketplace<br>
                    Seller Onboarding & Compliance Team
                </p>
                
                </body>
                </html>
                """.formatted(
                seller.getSellerName(),
                seller.getTempSellerRequestId(),
                seller.getSellerName(),
                comments,
                SUPPORT_TIAMEDS_COM,
                SUPPORT_TIAMEDS_COM
        );

        // Use HTML email sender
        emailService.sendHtmlMail(
                seller.getEmail(),
                "Seller Application Status: Rejected",
                body
        );
    }

    /**
     * Handles approval process for temporary seller.
     * Generates agreement PDF, credentials, and sends approval email.
     */
    private void handleApprovalForTempSeller(TempSeller tempSeller, String comments) {

        // 1️⃣ Generate Seller Agreement PDF (for record, optional to attach later)
        String pdfPath = pdfService.generateTempSellerAgreementPdf(tempSeller);

        // 2️⃣ Create Login Credentials (replace with secure generator in prod)
        String username = "test";
        String password = "test@123";

        // HTML Email Body
        String body = """
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                
                <p>Dear %s,</p>
                
                <p>
                    We are pleased to inform you that your <b>%s</b> registration on the 
                    <b>TiaMeds Marketplace platform</b> has been successfully reviewed and <b>approved</b> by our compliance team.
                </p>
                
                <p>
                    Your %s profile has now been activated, and you may begin accessing the 
                    TiaMeds Marketplace platform to onboard your products.
                </p>
                
                <p><b>Below are your account details:</b></p>
                
                <p>
                    Application ID: %s<br>
                    Registered Company Name: %s<br>
                    Registered Email ID: %s<br>
                    Platform Access Link: 
                    <a href="%s" style="color:#1a73e8; text-decoration:none;">Login to Platform</a>
                </p>
                
                <p>
                    <b>Admin Approval Comments:</b>
                </p>
                
                <p>%s</p>
                
                <p><b>Temporary Login Credentials:</b><br>
                    Username: %s<br>
                    Temporary Password: %s
                </p>
                
                <p>
                    For security purposes, you will be required to reset your password upon first login.
                </p>
                
                <p>
                    Your acceptance of the TiaMeds Marketplace Seller Policies has been recorded and is attached for your reference.
                </p>
                
                <p>
                    If you have any questions or require assistance, please contact our support team at 
                    <a href="mailto:%s">%s</a>.
                </p>
                
                <p>
                    We welcome you to the TiaMeds Marketplace platform and look forward to a successful association.
                </p>
                
                <p>
                    Warm regards,<br>
                    TiaMeds Marketplace<br>
                    Seller Onboarding & Compliance Team
                </p>
                
                </body>
                </html>
                """.formatted(
                tempSeller.getSellerName(),
                tempSeller.getSellerName(),
                tempSeller.getSellerName(),
                tempSeller.getTempSellerRequestId(),
                tempSeller.getSellerName(),
                tempSeller.getEmail(),
                LOGIN_URL,
                comments,
                username,
                password,
                SUPPORT_TIAMEDS_COM,
                SUPPORT_TIAMEDS_COM
        );

        // Send HTML Email
        emailService.sendHtmlMailWithAttachment(
                tempSeller.getEmail(),
                "Seller Application Approved – Welcome to TiaMeds Marketplace",
                body,
                pdfPath,
                "TiaMeds_Seller_Agreement.pdf"
        );

        // Mark Temp Seller as Approved
        tempSeller.setStatus("APPROVED");
        tempSellerRepo.save(tempSeller);
    }

//    private void handleApproval(TempSeller tempSeller) {
//
//        // 1️⃣ Move to Main Seller Table
//        Seller seller = mapToMainSeller(tempSeller);
//        sellerRepo.save(seller);
//
//        // 2️⃣ Generate PDF
//        String pdfPath = pdfService.generateSellerAgreementPdf(seller);
//
//        // 3️⃣ Create Login Credentials
//        String username = userService.generateUsername(seller);
//        String password = userService.generateRandomPassword();
//        userService.createUserAccount(seller, username, password);
//
//        // 4️⃣ Send Reset Password Link
//        String resetLink = userService.generateResetLink(seller);
//
//        // 5️⃣ Email
//        emailService.sendMail(
//                seller.getEmail(),
//                "Seller Approved 🎉",
//                "Your account is approved.\nUsername: " + username +
//                        "\nReset Password: " + resetLink +
//                        "\nAgreement PDF attached."
//        );
//
//        // 6️⃣ Mark Temp Seller as Completed
//        tempSeller.setStatus("APPROVED");
//        tempSellerRepo.save(tempSeller);
//    }
//
//    private Seller mapToMainSeller(TempSeller temp) {
//        Seller seller = new Seller();
//        seller.setName(temp.getName());
//        seller.setEmail(temp.getEmail());
//        seller.setPhone(temp.getPhone());
//        seller.setAddress(temp.getAddress());
//        return seller;
//    }

}
