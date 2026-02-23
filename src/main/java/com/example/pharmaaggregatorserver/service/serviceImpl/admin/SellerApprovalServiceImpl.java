package com.example.pharmaaggregatorserver.service.serviceImpl.admin;

import com.example.pharmaaggregatorserver.dto.seller.SellerApprovalRequestDTO;
import com.example.pharmaaggregatorserver.entity.master.ProductTypeMaster;
import com.example.pharmaaggregatorserver.entity.seller.*;
import com.example.pharmaaggregatorserver.entity.temp.seller.SellerTerms;
import com.example.pharmaaggregatorserver.entity.temp.seller.TempSeller;
import com.example.pharmaaggregatorserver.entity.temp.seller.TempSellerDocument;
import com.example.pharmaaggregatorserver.entity.temp.seller.TempSellerReviewHistory;
import com.example.pharmaaggregatorserver.exception.ApplicationException;
import com.example.pharmaaggregatorserver.exception.NotFoundException;
import com.example.pharmaaggregatorserver.repository.seller.SellerRepository;
import com.example.pharmaaggregatorserver.repository.temp.seller.SellerTermsRepository;
import com.example.pharmaaggregatorserver.repository.temp.seller.TempSellerRepository;
import com.example.pharmaaggregatorserver.repository.temp.seller.TempSellerReviewHistoryRepository;
import com.example.pharmaaggregatorserver.service.EmailService;
import com.example.pharmaaggregatorserver.service.PdfService;
import com.example.pharmaaggregatorserver.service.admin.SellerApprovalService;
import com.example.pharmaaggregatorserver.service.auth.UserCreationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerApprovalServiceImpl implements SellerApprovalService {

    public static final String SUPPORT_TIAMEDS_COM = "support@tiameds.com";
    public static final String LOGIN_URL = "https://pharma-aggregator-test.tiameds.ai/";

    private final TempSellerRepository tempSellerRepo;
    private final SellerRepository sellerRepo;
    private final EmailService emailService;
    private final PdfService pdfService;
    private final UserCreationService userCreationService;
    private final SellerTermsRepository sellerTermsRepository;
    private final TempSellerReviewHistoryRepository reviewHistoryRepository;

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
    @Transactional
    public void processReview(SellerApprovalRequestDTO request) {

        TempSeller tempSeller = tempSellerRepo.findById(request.getId())
                .orElseThrow(() -> new NotFoundException("Seller not found"));

        switch (request.getStatus().toUpperCase()) {

            case "CORRECTION" -> handleCorrection(tempSeller, request.getComments());

            case "REJECT" -> handleRejection(tempSeller, request.getComments());

//            case "ACCEPT" -> handleApprovalForTempSeller(tempSeller, request.getComments());

            case "ACCEPT" -> handleApproval(tempSeller, request.getComments());

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

        saveReviewHistory(seller, "CORRECTION_REQUIRED", comments);

        // Correction URL
        String correctionUrl = "https://testdomain.com/seller/correction/" + seller.getTempSellerId();

        // HTML Email Body
        String body = """
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                
                    <p>Dear %s,</p>
                
                    <p>
                        Thank you for submitting your application to onboard as a seller company on the 
                        <b>TiaMeds Marketplace platform</b> and Your Request ID is <b>%s</b>.
                    </p>
                
                    <p>
                        Our compliance team has reviewed your application and identified certain items that 
                        require <b>correction or additional information</b> before we can proceed with approval.
                    </p>
                
                    <p>Please review and address the following points:<br>
                    <b>%s</b></p>
                
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
                        Warm Regards,<br>
                        TiaMeds Marketplace<br>
                        Seller Onboarding & Compliance Team
                    </p>
                
                </body>
                </html>
                """.formatted(
                seller.getCoordinator().getName(),
                seller.getTempSellerRequestId(),
                comments,
                correctionUrl,
                correctionUrl,
                SUPPORT_TIAMEDS_COM,
                SUPPORT_TIAMEDS_COM
        );

        // IMPORTANT: Use HTML email method
        emailService.sendHtmlMail(
                seller.getCoordinator().getEmail(),
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
        saveReviewHistory(seller, "REJECTED", comments);

        String body = """
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                
                <p>Dear %s,</p>
                
                <p>
                    Thank you for submitting your application to onboard as a seller company on the 
                    <b>TiaMeds Marketplace platform</b> and Your Request ID is <b>%s</b>.
                </p>
                
                <p>
                    After a detailed review of your submitted information and documents, we regret to inform 
                    you that your <b>%s</b> registration application has been <b>rejected</b> due to the following reasons:
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
                    Warm Regards,<br>
                    TiaMeds Marketplace<br>
                    Seller Onboarding & Compliance Team
                </p>
                
                </body>
                </html>
                """.formatted(
                seller.getCoordinator().getName(),
                seller.getTempSellerRequestId(),
                seller.getSellerName(),
                comments,
                SUPPORT_TIAMEDS_COM,
                SUPPORT_TIAMEDS_COM
        );

        // Use HTML email sender
        emailService.sendHtmlMail(
                seller.getCoordinator().getEmail(),
                "Seller Application Status: Rejected",
                body
        );
    }

    /**
     * Handles approval process for temporary seller.
     * Generates agreement PDF, credentials, and sends approval email.
     */
//    private void handleApprovalForTempSeller(TempSeller tempSeller, String comments) {
//
//        // 1️⃣ Generate Seller Agreement PDF (for record, optional to attach later)
////        String pdfPath = pdfService.generateTempSellerAgreementPdf(tempSeller);
//
//        List<SellerTerms> sellerTerms = sellerTermsRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
//
//        String pdfPath = pdfService.generateSellerAgreementPdf(sellerTerms);
//
//        // 2️⃣ Create Login Credentials (replace with secure generator in prod)
//        String username = "test";
//        String password = "test@123";
//
//        // HTML Email Body
//        String body = """
//                <html>
//                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
//
//                <p>Dear %s,</p>
//
//                <p>
//                    We are pleased to inform you that your <b>%s</b> registration on the
//                    <b>TiaMeds Marketplace platform</b> has been successfully reviewed and <b>approved</b> by our compliance team.
//                </p>
//
//                <p>
//                    Your %s profile has now been activated, and you may begin accessing the
//                    TiaMeds Marketplace platform to onboard your products.
//                </p>
//
//                <p><b>Below are your account details:</b></p>
//
//                <p>
//                    Application ID: %s<br>
//                    Registered Company Name: %s<br>
//                    Registered Email ID: %s<br>
//                    Platform Access Link:
//                    <a href="%s" style="color:#1a73e8; text-decoration:none;">Login to Platform</a>
//                </p>
//
//                <p>
//                    <b>Admin Approval Comments:</b><br>
//                    %s
//                </p>
//
//                <p><b>Temporary Login Credentials:</b><br>
//                    Username: %s<br>
//                    Temporary Password: %s
//                </p>
//
//                <p>
//                    For security purposes, you will be required to reset your password upon first login.
//                </p>
//
//                <p>
//                    Your acceptance of the TiaMeds Marketplace Seller Policies has been recorded and is attached for your reference.
//                </p>
//
//                <p>
//                    If you have any questions or require assistance, please contact our support team at
//                    <a href="mailto:%s">%s</a>.
//                </p>
//
//                <p>
//                    We welcome you to the TiaMeds Marketplace platform and look forward to a successful association.
//                </p>
//
//                <p>
//                    Warm regards,<br>
//                    TiaMeds Marketplace<br>
//                    Seller Onboarding & Compliance Team
//                </p>
//
//                </body>
//                </html>
//                """.formatted(
//                tempSeller.getSellerName(),
//                tempSeller.getSellerName(),
//                tempSeller.getSellerName(),
//                tempSeller.getTempSellerRequestId(),
//                tempSeller.getSellerName(),
//                tempSeller.getEmail(),
//                LOGIN_URL,
//                comments,
//                username,
//                password,
//                SUPPORT_TIAMEDS_COM,
//                SUPPORT_TIAMEDS_COM
//        );
//
//        // Send HTML Email
//        emailService.sendHtmlMailWithAttachment(
//                tempSeller.getEmail(),
//                "Seller Application Approved – Welcome to TiaMeds Marketplace",
//                body,
//                pdfPath,
//                "TiaMeds_Seller_Agreement.pdf"
//        );
//
//        // Mark Temp Seller as Approved
//        tempSeller.setStatus("APPROVED");
//        tempSellerRepo.save(tempSeller);
//    }

    /**
     * Handles the full approval flow:
     * 1. Copies TempSeller data to the main Seller table
     * 2. Generates a unique Seller ID
     * 3. Generates the seller agreement PDF
     * 4. Sends approval email with credentials and seller ID
     * 5. Marks TempSeller status as APPROVED
     */
    private void handleApproval(TempSeller tempSeller, String comments) {

        // 1️⃣ Migrate data from temp → main seller table
        Seller approvedSeller = mapAndPersistSeller(tempSeller);
        saveReviewHistory(tempSeller, "APPROVED", comments);

        // 2️⃣ Generate Seller Agreement PDF
        List<SellerTerms> sellerTerms = sellerTermsRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));

        String pdfPath = pdfService.generateSellerAgreementPdf(sellerTerms);

        // 3️⃣ Create User account with auto-generated secure password
        String coordinatorEmail = tempSeller.getCoordinator().getEmail();
        UserCreationService.UserCreationResult result =
                userCreationService.createSellerUser(coordinatorEmail);

        // username = coordinator email, plainTempPassword = to be emailed (never stored)
        String username = coordinatorEmail;
        String password = result.plainTempPassword();

        // 4️⃣ Link the created user to the approved seller
        approvedSeller.setUser(result.user());
        sellerRepo.save(approvedSeller);

        // 4️⃣ Build HTML Email Body
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
                    Request ID: %s<br>
                    Seller ID: <b>%s</b><br>
                    Registered Company Name: %s<br>
                    Registered Email ID: %s<br>
                    TiaMeds Marketplace Access Link:
                    <a href="%s" style="color:#1a73e8; text-decoration:none;">Login to Platform</a><br>
                    <b>%s</b>
                </p>
                
                <p>
                Please note that the Seller ID %s is your %s’s unique 
                identification number on the TiaMeds Marketplace platform. This Seller ID will be used to 
                identify your company across the TiaMeds Marketplace platform for all transactions. Kindly 
                refer to and quote your Seller ID in all future correspondence with the TiaMeds Marketplace 
                team.
                </p>
                
                <p><b>Please find the below temporary login credentials:</b><br>
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
                approvedSeller.getCoordinator().getName(),
                approvedSeller.getSellerName(),
                approvedSeller.getSellerName(),
                tempSeller.getTempSellerRequestId(),
                approvedSeller.getSellerId(),
                approvedSeller.getSellerName(),
                approvedSeller.getCoordinator().getEmail(),
                LOGIN_URL,
                comments,
                approvedSeller.getSellerId(),
                approvedSeller.getSellerName(),
                username,
                password,
                SUPPORT_TIAMEDS_COM,
                SUPPORT_TIAMEDS_COM
        );

        // 5️⃣ Send approval email with PDF agreement attached
        emailService.sendHtmlMailWithAttachment(
                tempSeller.getCoordinator().getEmail(),
                "Seller Application Approved – Welcome to TiaMeds Marketplace",
                body,
                pdfPath,
                "TiaMeds_Seller_Agreement.pdf"
        );

        // 6️⃣ Mark TempSeller as APPROVED
        tempSeller.setStatus("APPROVED");
        tempSellerRepo.save(tempSeller);
    }

    /**
     * Maps a TempSeller (and all its child entities) to the main Seller table.
     * Generates a unique seller ID before saving.
     */
    private Seller mapAndPersistSeller(TempSeller temp) {
        String sellerId = generateSellerId(temp);

        // ✅ Force load lazy collections before session closes
        List<ProductTypeMaster> productTypes = new ArrayList<>(temp.getProductTypes());

        Seller seller = new Seller();
        seller.setSellerId(sellerId);
        seller.setSellerName(temp.getSellerName());
        seller.setPhone(temp.getPhone());
        seller.setPhoneVerified(temp.isPhoneVerified());
        seller.setEmail(temp.getEmail());
        seller.setEmailVerified(temp.isEmailVerified());
        seller.setWebsite(temp.getWebsite());
        seller.setTermsAccepted(temp.isTermsAccepted());
        seller.setCompanyType(temp.getCompanyType());
        seller.setSellerType(temp.getSellerType());
        seller.setProductTypes(productTypes);
        seller.setStatus("APPROVED");
        seller.setCreatedBy("SYSTEM");
        seller.setUpdatedBy("SYSTEM");

        // Save seller first so child entities can reference it
        Seller savedSeller = sellerRepo.save(seller);

        // ── Address ──
        if (temp.getAddress() != null) {
            SellerAddress address = new SellerAddress();
            address.setSeller(savedSeller);
            address.setState(temp.getAddress().getState());
            address.setDistrict(temp.getAddress().getDistrict());
            address.setTaluka(temp.getAddress().getTaluka());
            address.setCity(temp.getAddress().getCity());
            address.setStreet(temp.getAddress().getStreet());
            address.setBuildingNo(temp.getAddress().getBuildingNo());
            address.setLandmark(temp.getAddress().getLandmark());
            address.setPinCode(temp.getAddress().getPinCode());
            address.setCreatedBy("SYSTEM");
            address.setUpdatedBy("SYSTEM");
            savedSeller.setAddress(address);
        }

        // ── Coordinator ──
        if (temp.getCoordinator() != null) {
            SellerCoordinator coordinator = new SellerCoordinator();
            coordinator.setSeller(savedSeller);
            coordinator.setName(temp.getCoordinator().getName());
            coordinator.setDesignation(temp.getCoordinator().getDesignation());
            coordinator.setEmail(temp.getCoordinator().getEmail());
            coordinator.setEmailVerified(temp.getCoordinator().isEmailVerified());
            coordinator.setMobile(temp.getCoordinator().getMobile());
            coordinator.setPhoneVerified(temp.getCoordinator().isPhoneVerified());
            coordinator.setCreatedBy("SYSTEM");
            coordinator.setUpdatedBy("SYSTEM");
            savedSeller.setCoordinator(coordinator);
        }

        // ── Bank Details ──
        if (temp.getBankDetails() != null) {
            SellerBankDetails bankDetails = new SellerBankDetails();
            bankDetails.setSeller(savedSeller);
            bankDetails.setBankName(temp.getBankDetails().getBankName());
            bankDetails.setBranch(temp.getBankDetails().getBranch());
            bankDetails.setIfscCode(temp.getBankDetails().getIfscCode());
            bankDetails.setAccountNumber(temp.getBankDetails().getAccountNumber());
            bankDetails.setAccountHolderName(temp.getBankDetails().getAccountHolderName());
            bankDetails.setBankDocumentFileUrl(temp.getBankDetails().getBankDocumentFileUrl());
            bankDetails.setBankDocumentVerified(temp.getBankDetails().isBankDocumentVerified());
            bankDetails.setCreatedBy("SYSTEM");
            bankDetails.setUpdatedBy("SYSTEM");
            savedSeller.setBankDetails(bankDetails);
        }

        // ── GST ──
        SellerGST gst = new SellerGST();
        gst.setSeller(savedSeller);
        gst.setGstNumber(temp.getGstNumber());
        gst.setGstFileUrl(temp.getGstFileUrl());
        gst.setGstVerified(temp.isGstVerified());
        savedSeller.setSellerGST(gst);

        // ── Documents ──
        if (temp.getDocuments() != null && !temp.getDocuments().isEmpty()) {
            for (TempSellerDocument tempDoc : temp.getDocuments()) {
                SellerDocument doc = new SellerDocument();
                doc.setSeller(savedSeller);
                doc.setProductTypes(tempDoc.getProductTypes());
                doc.setDocumentNumber(tempDoc.getDocumentNumber());
                doc.setDocumentFileUrl(tempDoc.getDocumentFileUrl());
                doc.setDocumentVerified(tempDoc.isDocumentVerified());
                doc.setCreatedBy("SYSTEM");
                doc.setUpdatedBy("SYSTEM");
                savedSeller.getDocuments().add(doc);
            }
        }

        // Final save to persist all cascaded children
        return sellerRepo.save(savedSeller);
    }

    /**
     * Generates a unique Seller ID in the format:
     * [2 chars from seller name][3 chars from seller type abbreviation][4-digit global sequence]
     * <p>
     * Example: CIPLA + MFG → CI + MFG + 0001 = CIMFG0001
     * <p>
     * The 4-digit sequence is global (not per name/type) and always increments
     * from the highest existing sequence number across all seller IDs.
     */
    private String generateSellerId(TempSeller tempSeller) {
        String namePart = tempSeller.getSellerName()
                .replaceAll("\\s+", "")
                .toUpperCase();
        namePart = namePart.length() >= 2 ? namePart.substring(0, 2) : namePart;

        String typePart = tempSeller.getSellerType()
                .getSellerTypeAbbreviation()
                .replaceAll("\\s+", "")
                .toUpperCase();
        typePart = typePart.length() >= 3 ? typePart.substring(0, 3) : typePart;

        // Acquire PostgreSQL advisory lock before reading max sequence to prevent
        // duplicate seller IDs under concurrent admin approvals (multi-node safe)
        sellerRepo.acquireSellerIdLock();
        // Find the current max sequence number across ALL seller IDs
        Integer nextSequence = sellerRepo.findMaxSellerSequence() + 1;

        String sequencePart = String.format("%04d", nextSequence);

        return namePart + typePart + sequencePart;
    }

    private void saveReviewHistory(TempSeller seller, String status, String comments) {
        TempSellerReviewHistory history = TempSellerReviewHistory.builder()
                .tempSeller(seller)
                .status(status)
                .comments(comments)
                .reviewedBy("ADMIN") // replace with actual admin context later
                .reviewedAt(LocalDateTime.now())
                .build();
        reviewHistoryRepository.save(history);
    }

}
