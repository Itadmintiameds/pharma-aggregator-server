package com.example.pharmaaggregatorserver.service.serviceImpl.admin;

import com.example.pharmaaggregatorserver.dto.seller.SellerApprovalRequestDTO;
import com.example.pharmaaggregatorserver.entity.auth.User;
import com.example.pharmaaggregatorserver.entity.master.ProductTypeMaster;
import com.example.pharmaaggregatorserver.entity.seller.*;
import com.example.pharmaaggregatorserver.entity.temp.seller.SellerTerms;
import com.example.pharmaaggregatorserver.entity.temp.seller.TempSeller;
import com.example.pharmaaggregatorserver.entity.temp.seller.TempSellerDocument;
import com.example.pharmaaggregatorserver.entity.temp.seller.TempSellerReviewHistory;
import com.example.pharmaaggregatorserver.exception.ApplicationException;
import com.example.pharmaaggregatorserver.exception.NotFoundException;
import com.example.pharmaaggregatorserver.repository.master.ProductTypeMasterRepository;
import com.example.pharmaaggregatorserver.repository.seller.SellerRepository;
import com.example.pharmaaggregatorserver.repository.temp.seller.SellerTermsRepository;
import com.example.pharmaaggregatorserver.repository.temp.seller.TempSellerRepository;
import com.example.pharmaaggregatorserver.repository.temp.seller.TempSellerReviewHistoryRepository;
import com.example.pharmaaggregatorserver.service.EmailService;
import com.example.pharmaaggregatorserver.service.PdfService;
import com.example.pharmaaggregatorserver.service.S3Service;
import com.example.pharmaaggregatorserver.service.admin.SellerApprovalService;
import com.example.pharmaaggregatorserver.service.auth.UserCreationService;
import com.example.pharmaaggregatorserver.service.temp.seller.TempSellerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerApprovalServiceImpl implements SellerApprovalService {

    public static final String SUPPORT_TIAMEDS_COM = "support@tiameds.com";

    @Value("${app.frontend-url}")
    public String LOGIN_URL;

    @Value("${app.admin-frontend-url}")
    public String ADMIN_FRONTEND_URL;

    private final TempSellerRepository tempSellerRepo;
    private final SellerRepository sellerRepo;
    private final EmailService emailService;
    private final PdfService pdfService;
    private final UserCreationService userCreationService;
    private final SellerTermsRepository sellerTermsRepository;
    private final TempSellerReviewHistoryRepository reviewHistoryRepository;
    private final S3Service s3Service;
    private final ProductTypeMasterRepository productTypeMasterRepository;
    private final TempSellerService tempSellerService;

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
        String correctionUrl = ADMIN_FRONTEND_URL + "/SellerCorrection/[requestId]?sellerId=" + seller.getTempSellerId();

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
//        tempSellerService.deleteTempSeller(seller.getTempSellerId());
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

        // 1️⃣ Create User FIRST
        String coordinatorEmail = tempSeller.getCoordinator().getEmail();
        UserCreationService.UserCreationResult result =
                userCreationService.createSellerUser(coordinatorEmail);
        String username = coordinatorEmail;
        String password = result.plainTempPassword();

        // 2️⃣ Migrate data from temp → main seller table (pass user)
        Seller approvedSeller = mapAndPersistSeller(tempSeller, result.user());
        saveReviewHistory(tempSeller, "APPROVED", comments);

        // 3️⃣ Generate Seller Agreement PDF
        SellerTerms terms = sellerTermsRepository.findByTermText("Seller-Terms")
                .orElseThrow(() -> new ApplicationException("No seller terms PDF URL configured"));

        byte[] pdfBytes = fetchPdfFromUrl(terms.getTermsUrl());

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
                Please note that the Seller ID %s is your %s's unique 
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
                pdfBytes,                         // byte[]
                "TiaMeds_Seller_Agreement.pdf"
        );

        // 6️⃣ Mark TempSeller as APPROVED
        tempSeller.setStatus("APPROVED");
        tempSellerRepo.save(tempSeller);
    }

    /**
     * PHASE 1: Maps all TempSeller data → Seller table using temp S3 URLs as-is.
     * PHASE 2: Once DB save is confirmed, migrates every image from
     * tempsellers/{REQ_ID}/... → sellers/{SELLER_ID}/...
     * then deletes the old temp S3 objects.
     * <p>
     * If any image migration fails it is logged but does NOT roll back the approval.
     */
    private Seller mapAndPersistSeller(TempSeller temp, User user) {
        String sellerId = generateSellerId(temp);

        // ✅ Force load lazy collections before session closes
        List<ProductTypeMaster> productTypes = new ArrayList<>(temp.getProductTypes());

        // ═══════════════════════════════════════════════════════════════
        // PHASE 1 — Build and persist all entities with existing temp URLs
        // ═══════════════════════════════════════════════════════════════

        Seller seller = new Seller();
        seller.setSellerId(sellerId);
        seller.setUser(user);
        seller.setSellerName(temp.getSellerName());
        seller.setPhone(temp.getPhone());
        seller.setPhoneVerified(temp.isPhoneVerified());
        seller.setEmail(temp.getEmail());
        seller.setEmailVerified(temp.isEmailVerified());
        seller.setWebsite(temp.getWebsite());
        seller.setTermsAccepted(temp.isTermsAccepted());
        seller.setCompanyRegistrationCertificateUrl(temp.getCompanyRegistrationCertificateUrl());
        seller.setCompanyRegistrationCertificateVerified(temp.isCompanyRegistrationCertificateVerified());
        seller.setCompanyType(temp.getCompanyType());
        seller.setSellerType(temp.getSellerType());
        seller.setProductTypes(productTypes);
        seller.setStatus("APPROVED");
        seller.setCreatedBy("SYSTEM");
        seller.setUpdatedBy("SYSTEM");
        // Seller image: set temp URL for now — will be replaced in Phase 2
        seller.setSellerImageUrl(temp.getSellerImageUrl());

        // Save seller first so child entities can reference it
        Seller savedSeller = sellerRepo.save(seller);

        // ── Address ──────────────────────────────────────────────────────
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

        // ── Bank Details — store temp URL for now ─────────────────────────
        if (temp.getBankDetails() != null) {
            SellerBankDetails bankDetails = new SellerBankDetails();
            bankDetails.setSeller(savedSeller);
            bankDetails.setBankName(temp.getBankDetails().getBankName());
            bankDetails.setBranch(temp.getBankDetails().getBranch());
            bankDetails.setIfscCode(temp.getBankDetails().getIfscCode());
            bankDetails.setAccountNumber(temp.getBankDetails().getAccountNumber());
            bankDetails.setAccountHolderName(temp.getBankDetails().getAccountHolderName());
            bankDetails.setBankDocumentFileUrl(temp.getBankDetails().getBankDocumentFileUrl()); // temp URL for now
            bankDetails.setBankDocumentVerified(temp.getBankDetails().isBankDocumentVerified());
            bankDetails.setCreatedBy("SYSTEM");
            bankDetails.setUpdatedBy("SYSTEM");
            savedSeller.setBankDetails(bankDetails);
        }

        // ── GST — store temp URL for now ──────────────────────────────────
        SellerGST gst = new SellerGST();
        gst.setSeller(savedSeller);
        gst.setGstNumber(temp.getGstNumber());
        gst.setGstFileUrl(temp.getGstFileUrl()); // temp URL for now
        gst.setGstVerified(temp.isGstVerified());
        savedSeller.setSellerGST(gst);

        // ── Documents — store temp URLs for now ───────────────────────────
        if (temp.getDocuments() != null && !temp.getDocuments().isEmpty()) {
            for (TempSellerDocument tempDoc : temp.getDocuments()) {
                SellerDocument doc = new SellerDocument();
                doc.setSeller(savedSeller);
                doc.setProductTypes(tempDoc.getProductTypes());
                doc.setDocumentNumber(tempDoc.getDocumentNumber());
                doc.setDocumentFileUrl(tempDoc.getDocumentFileUrl()); // temp URL for now
                doc.setDocumentVerified(tempDoc.isDocumentVerified());
                doc.setLicenseIssueDate(tempDoc.getLicenseIssueDate());
                doc.setLicenseExpiryDate(tempDoc.getLicenseExpiryDate());
                doc.setLicenseIssuingAuthority(tempDoc.getLicenseIssuingAuthority());
                doc.setLicenseStatus(tempDoc.getLicenseStatus());
                doc.setCreatedBy("SYSTEM");
                doc.setUpdatedBy("SYSTEM");
                savedSeller.getDocuments().add(doc);
            }
        }

        // Persist everything with temp URLs
        Seller fullyPersistedSeller = sellerRepo.save(savedSeller);
        log.info("✅ Phase 1 complete — Seller persisted with id={}", fullyPersistedSeller.getSellerId());

        // ═══════════════════════════════════════════════════════════════
        // PHASE 2 — Migrate all S3 images to sellers/{SELLER_ID}/...
        //           then delete the old temp objects
        // ═══════════════════════════════════════════════════════════════
        migrateAllSellerImages(temp, fullyPersistedSeller);

        return fullyPersistedSeller;
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

    /**
     * Migrates every file for an approved seller from the temp S3 folder
     * to the permanent seller folder, updates the DB URL, then deletes the temp file.
     * <p>
     * Folder mapping:
     * tempsellers/{REQ_ID}/sellerimage/... → sellers/{SELLER_ID}/sellerimage/...
     * tempsellers/{REQ_ID}/gst/...         → sellers/{SELLER_ID}/gst/...
     * tempsellers/{REQ_ID}/bankdocument/... → sellers/{SELLER_ID}/bankdocument/...
     * tempsellers/{REQ_ID}/licenses/...    → sellers/{SELLER_ID}/licenses/...
     */
    private void migrateAllSellerImages(TempSeller temp, Seller seller) {

        String sellerId = seller.getSellerId();
        log.info("🚀 Phase 2 — Starting S3 migration for sellerId={}", sellerId);

        // ── 1. Seller profile image ──────────────────────────────────────
        if (hasUrl(temp.getSellerImageUrl())) {
            String newUrl = copyToSellerFolder(
                    temp.getSellerImageUrl(),
                    sellerId,
                    "sellerimage"
            );
            if (newUrl != null) {
                seller.setSellerImageUrl(newUrl);
                sellerRepo.save(seller);
                deleteOldFile(temp.getSellerImageUrl());
                log.info("✅ Seller image migrated for sellerId={}", sellerId);
            }
        }

        // ── 2. GST document ──────────────────────────────────────────────
        if (seller.getSellerGST() != null && hasUrl(seller.getSellerGST().getGstFileUrl())) {
            String newUrl = copyToSellerFolder(
                    temp.getGstFileUrl(),
                    sellerId,
                    "gst"
            );
            if (newUrl != null) {
                seller.getSellerGST().setGstFileUrl(newUrl);
                sellerRepo.save(seller);
                deleteOldFile(temp.getGstFileUrl());
                log.info("✅ GST document migrated for sellerId={}", sellerId);
            }
        }

        // ── 3. Bank document ─────────────────────────────────────────────
        if (seller.getBankDetails() != null && hasUrl(temp.getBankDetails().getBankDocumentFileUrl())) {
            String newUrl = copyToSellerFolder(
                    temp.getBankDetails().getBankDocumentFileUrl(),
                    sellerId,
                    "bankdocument"
            );
            if (newUrl != null) {
                seller.getBankDetails().setBankDocumentFileUrl(newUrl);
                sellerRepo.save(seller);
                deleteOldFile(temp.getBankDetails().getBankDocumentFileUrl());
                log.info("✅ Bank document migrated for sellerId={}", sellerId);
            }
        }

        // ── 4. License documents ─────────────────────────────────────────
        if (temp.getDocuments() != null && !temp.getDocuments().isEmpty()) {
            List<TempSellerDocument> tempDocs = temp.getDocuments();
            List<SellerDocument> sellerDocs = seller.getDocuments();

            for (int i = 0; i < tempDocs.size(); i++) {
                String tempUrl = tempDocs.get(i).getDocumentFileUrl();
                if (!hasUrl(tempUrl)) continue;

                String newUrl = copyToSellerFolder(tempUrl, sellerId, "licenses");
                if (newUrl != null) {
                    sellerDocs.get(i).setDocumentFileUrl(newUrl);
                    deleteOldFile(tempUrl);
                    log.info("✅ License document [{}] migrated for sellerId={}", i, sellerId);
                }
            }
            sellerRepo.save(seller);
        }

        // ── 5. Company Registration Certificate ──────────────────────────────────────
        if (hasUrl(temp.getCompanyRegistrationCertificateUrl())) {
            String newUrl = copyToSellerFolder(
                    temp.getCompanyRegistrationCertificateUrl(),
                    sellerId,
                    "companyregistrationcertificate"
            );
            if (newUrl != null) {
                seller.setCompanyRegistrationCertificateUrl(newUrl);
                sellerRepo.save(seller);
                deleteOldFile(temp.getCompanyRegistrationCertificateUrl());
                log.info("✅ Company Registration Certificate migrated for sellerId={}", sellerId);
            }
        }

        log.info("✅ Phase 2 complete — All S3 images migrated for sellerId={}", sellerId);
    }

    /**
     * Copies a file from its current S3 URL to:
     * sellers/{sellerId}/{folder}/{original-filename}
     * <p>
     * Returns the new S3 URL on success, or null if the copy fails.
     */
    private String copyToSellerFolder(String sourceUrl, String sellerId, String folder) {
        try {
            String oldKey = s3Service.extractKeyFromUrl(sourceUrl);
            String fileName = oldKey.substring(oldKey.lastIndexOf('/') + 1);
            String newKey = String.format("sellers/%s/%s/%s", sellerId, folder, fileName);

            // Derive content type from the file extension in the key
            String contentType = resolveContentType(newKey);

            Resource resource = s3Service.getFile(oldKey);
            return s3Service.uploadFileFromResource(newKey, resource, contentType);

        } catch (Exception e) {
            log.error("❌ Failed to copy S3 file from url={} to sellers/{}/{}: {}",
                    sourceUrl, sellerId, folder, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Infers the MIME content type from the S3 key file extension.
     * Defaults to application/octet-stream if extension is unrecognised.
     */
    private String resolveContentType(String key) {
        if (key == null) return "application/octet-stream";
        String lower = key.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".pdf")) return "application/pdf";
        return "application/octet-stream";
    }

    /**
     * Deletes an old temp S3 file. Logs a warning on failure but never throws.
     */
    private void deleteOldFile(String url) {
        try {
            s3Service.deleteFile(s3Service.extractKeyFromUrl(url));
        } catch (Exception e) {
            log.warn("⚠️ Could not delete old S3 file url={}: {}", url, e.getMessage());
        }
    }

    /**
     * Returns true when a URL is a real S3 URL (not null, blank, or PENDING placeholder).
     */
    private boolean hasUrl(String url) {
        return url != null && !url.isBlank() && !"PENDING".equalsIgnoreCase(url.trim());
    }

    private byte[] fetchPdfFromUrl(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10_000);
            connection.setReadTimeout(30_000);

            int status = connection.getResponseCode();
            if (status != 200) {
                throw new ApplicationException("Failed to fetch PDF from S3. HTTP status: " + status);
            }

            try (InputStream in = connection.getInputStream()) {
                return in.readAllBytes();
            }
        } catch (IOException e) {
            throw new ApplicationException("Error downloading seller agreement PDF: " + e.getMessage());
        }
    }
}
