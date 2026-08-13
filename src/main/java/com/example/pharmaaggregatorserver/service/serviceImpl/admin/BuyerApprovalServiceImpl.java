package com.example.pharmaaggregatorserver.service.serviceImpl.admin;

import com.example.pharmaaggregatorserver.dto.buyer.BuyerApprovalRequestDTO;
import com.example.pharmaaggregatorserver.dto.buyer.BuyerApprovalResultDTO;
import com.example.pharmaaggregatorserver.entity.buyer.Buyer;
import com.example.pharmaaggregatorserver.entity.buyer.BuyerAddress;
import com.example.pharmaaggregatorserver.entity.buyer.BuyerContact;
import com.example.pharmaaggregatorserver.entity.buyer.BuyerDocument;
import com.example.pharmaaggregatorserver.entity.buyer.BuyerUser;
import com.example.pharmaaggregatorserver.entity.temp.buyer.TempBuyer;
import com.example.pharmaaggregatorserver.entity.temp.buyer.TempBuyerDocument;
import com.example.pharmaaggregatorserver.entity.temp.buyer.TempBuyerReviewHistory;
import com.example.pharmaaggregatorserver.entity.temp.buyer.TempBuyerStatus;
import com.example.pharmaaggregatorserver.exception.ApplicationException;
import com.example.pharmaaggregatorserver.exception.NotFoundException;
import com.example.pharmaaggregatorserver.repository.buyer.BuyerRepository;
import com.example.pharmaaggregatorserver.repository.temp.buyer.TempBuyerRepository;
import com.example.pharmaaggregatorserver.repository.temp.buyer.TempBuyerReviewHistoryRepository;
import com.example.pharmaaggregatorserver.service.EmailService;
import com.example.pharmaaggregatorserver.service.S3Service;
import com.example.pharmaaggregatorserver.service.admin.BuyerApprovalService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Mirrors service.serviceImpl.admin.SellerApprovalServiceImpl's
 * processReview() structure for the buyer onboarding flow.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BuyerApprovalServiceImpl implements BuyerApprovalService {

    public static final String SUPPORT_TIAMEDS_COM = "support@tiameds.com";

    @Value("${app.frontend-url}")
    public String LOGIN_URL;

    private final TempBuyerRepository tempBuyerRepo;
    private final BuyerRepository buyerRepo;
    private final EmailService emailService;
    private final TempBuyerReviewHistoryRepository reviewHistoryRepository;
    private final S3Service s3Service;

    @Override
    @Transactional
    public BuyerApprovalResultDTO processReview(BuyerApprovalRequestDTO request) {

        TempBuyer tempBuyer = tempBuyerRepo.findById(request.getId())
                .orElseThrow(() -> new NotFoundException("Buyer not found"));

        Buyer approvedBuyer = null;

        switch (request.getStatus().toUpperCase()) {

            case "CORRECTION" -> handleCorrection(tempBuyer, request.getComments());

            case "REJECT" -> handleRejection(tempBuyer, request.getComments());

            case "ACCEPT" -> approvedBuyer = handleApproval(tempBuyer, request.getComments());

            default -> throw new ApplicationException("Invalid Status");
        }

        return BuyerApprovalResultDTO.builder()
                .tempBuyerId(tempBuyer.getTempBuyerId())
                .buyerUserId(tempBuyer.getUser() != null ? tempBuyer.getUser().getBuyerUserId() : null)
                .buyerId(approvedBuyer != null ? approvedBuyer.getBuyerId() : null)
                .status(tempBuyer.getStatus())
                .build();
    }

    private void handleCorrection(TempBuyer buyer, String comments) {
        buyer.setStatus(TempBuyerStatus.CORRECTION_REQUIRED);
        tempBuyerRepo.save(buyer);
        saveReviewHistory(buyer, TempBuyerStatus.CORRECTION_REQUIRED, comments);

        String contactName = buyer.getContact() != null ? buyer.getContact().getName() : "";
        String contactEmail = buyer.getContact() != null ? buyer.getContact().getEmail() : null;

        String body = """
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">

                    <p>Dear %s,</p>

                    <p>
                        Thank you for submitting your application to onboard as a buyer on the
                        <b>TiaMeds Marketplace platform</b> and Your Request ID is <b>%s</b>.
                    </p>

                    <p>
                        Our compliance team has reviewed your application and identified certain items that
                        require <b>correction or additional information</b> before we can proceed with approval.
                    </p>

                    <p>Please review and address the following points:<br>
                    <b>%s</b></p>

                    <p>
                        Kindly log in to your application and update the required information.
                    </p>

                    <p>
                        Once the corrections are submitted, your application will be re-evaluated by our compliance team.
                    </p>

                    <p>
                        For any assistance, please contact our support team at
                        <a href="mailto:%s">%s</a>.
                    </p>

                    <p>
                        Warm Regards,<br>
                        TiaMeds Marketplace<br>
                        Buyer Onboarding &amp; Compliance Team
                    </p>

                </body>
                </html>
                """.formatted(
                contactName,
                buyer.getTempBuyerRequestId(),
                comments,
                SUPPORT_TIAMEDS_COM,
                SUPPORT_TIAMEDS_COM
        );

        if (contactEmail != null && !contactEmail.isBlank()) {
            emailService.sendHtmlMail(contactEmail, "Action Required: Buyer Application Correction", body);
        } else {
            log.warn("No contact email found for TempBuyer id={}. Correction email not sent.", buyer.getTempBuyerId());
        }
    }

    private void handleRejection(TempBuyer buyer, String comments) {
        buyer.setStatus(TempBuyerStatus.REJECTED);
        tempBuyerRepo.save(buyer);
        saveReviewHistory(buyer, TempBuyerStatus.REJECTED, comments);

        String contactName = buyer.getContact() != null ? buyer.getContact().getName() : "";
        String contactEmail = buyer.getContact() != null ? buyer.getContact().getEmail() : null;

        String body = """
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">

                <p>Dear %s,</p>

                <p>
                    Thank you for submitting your application to onboard as a buyer on the
                    <b>TiaMeds Marketplace platform</b> and Your Request ID is <b>%s</b>.
                </p>

                <p>
                    After a detailed review of your submitted information and documents, we regret to inform
                    you that your <b>%s</b> registration application has been <b>rejected</b> due to the following reasons:
                </p>

                <p><b>%s</b></p>

                <p>
                    You may submit a fresh application after addressing the above-mentioned issues and
                    ensuring that all required information and documents are accurate and complete.
                </p>

                <p>
                    For any clarification, please contact our support team at
                    <a href="mailto:%s">%s</a>.
                </p>

                <p>
                    Warm Regards,<br>
                    TiaMeds Marketplace<br>
                    Buyer Onboarding &amp; Compliance Team
                </p>

                </body>
                </html>
                """.formatted(
                contactName,
                buyer.getTempBuyerRequestId(),
                buyer.getOrganizationName(),
                comments,
                SUPPORT_TIAMEDS_COM,
                SUPPORT_TIAMEDS_COM
        );

        if (contactEmail != null && !contactEmail.isBlank()) {
            emailService.sendHtmlMail(contactEmail, "Buyer Application Status: Rejected", body);
        } else {
            log.warn("No contact email found for TempBuyer id={}. Rejection email not sent.", buyer.getTempBuyerId());
        }
    }

    private Buyer handleApproval(TempBuyer tempBuyer, String comments) {

        BuyerUser buyerUser = tempBuyer.getUser();
        if (buyerUser == null) {
            throw new ApplicationException(
                    "Cannot approve buyer request " + tempBuyer.getTempBuyerRequestId()
                            + ": this registration has no linked login account. "
                            + "Link a BuyerUser to this TempBuyer before approving.");
        }

        Buyer approvedBuyer = mapAndPersistBuyer(tempBuyer, buyerUser);

        saveReviewHistory(tempBuyer, TempBuyerStatus.APPROVED, comments);

        // Mark TempBuyer as APPROVED now, before the best-effort email/S3
        // migration steps below — a slow SMTP/S3 outage should never leave
        // the buyer half-approved.
        tempBuyer.setStatus(TempBuyerStatus.APPROVED);
        tempBuyerRepo.save(tempBuyer);

        sendApprovalEmail(tempBuyer, approvedBuyer, comments);

        migrateAllBuyerImages(tempBuyer, approvedBuyer);

        return approvedBuyer;
    }

    private void sendApprovalEmail(TempBuyer tempBuyer, Buyer approvedBuyer, String comments) {
        try {
            String contactName = approvedBuyer.getContact() != null ? approvedBuyer.getContact().getName() : "";
            String contactEmail = approvedBuyer.getContact() != null ? approvedBuyer.getContact().getEmail() : null;

            if (contactEmail == null || contactEmail.isBlank()) {
                log.warn("No contact email found for approved Buyer id={}. Approval email not sent.", approvedBuyer.getBuyerId());
                return;
            }

            String body = """
                    <html>
                    <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">

                    <p>Dear %s,</p>

                    <p>
                        We are pleased to inform you that your <b>%s</b> registration on the
                        <b>TiaMeds Marketplace platform</b> has been successfully reviewed and <b>approved</b> by our compliance team.
                    </p>

                    <p><b>Below are your account details:</b></p>

                    <p>
                        Request ID: %s<br>
                        Buyer ID: <b>%s</b><br>
                        Registered Organization Name: %s<br>
                        Registered Email ID: %s<br>
                        TiaMeds Marketplace Access Link:
                        <a href="%s" style="color:#1a73e8; text-decoration:none;">Login to Platform</a><br>
                        <b>%s</b>
                    </p>

                    <p>
                    Please note that the Buyer ID %s is your organization's unique identification number on
                    the TiaMeds Marketplace platform. Kindly refer to and quote your Buyer ID in all future
                    correspondence with the TiaMeds Marketplace team.
                    </p>

                    <p>
                        You can log in using the email and password you created during signup.
                    </p>

                    <p>
                        If you have any questions or require assistance, please contact our support team at
                        <a href="mailto:%s">%s</a>.
                    </p>

                    <p>
                        Warm regards,<br>
                        TiaMeds Marketplace<br>
                        Buyer Onboarding &amp; Compliance Team
                    </p>

                    </body>
                    </html>
                    """.formatted(
                    contactName,
                    approvedBuyer.getOrganizationName(),
                    tempBuyer.getTempBuyerRequestId(),
                    approvedBuyer.getBuyerId(),
                    approvedBuyer.getOrganizationName(),
                    contactEmail,
                    LOGIN_URL,
                    comments,
                    approvedBuyer.getBuyerId(),
                    SUPPORT_TIAMEDS_COM,
                    SUPPORT_TIAMEDS_COM
            );

            emailService.sendHtmlMail(contactEmail, "Buyer Application Approved – Welcome to TiaMeds Marketplace", body);
        } catch (Exception e) {
            log.error("Buyer {} (request {}) was approved successfully, but sending the approval email failed: {}",
                    approvedBuyer.getBuyerId(), tempBuyer.getTempBuyerRequestId(), e.getMessage(), e);
        }
    }

    /**
     * PHASE 1: Maps all TempBuyer data → Buyer table using temp S3 URLs as-is.
     */
    private Buyer mapAndPersistBuyer(TempBuyer temp, BuyerUser user) {
        String buyerId = generateBuyerId(temp);

        Buyer buyer = new Buyer();
        buyer.setBuyerId(buyerId);
        buyer.setTempBuyerId(temp.getTempBuyerId());
        buyer.setApprovedAt(LocalDateTime.now());
        buyer.setUser(user);
        buyer.setOrganizationName(temp.getOrganizationName());
        buyer.setOrgLogoUrl(temp.getOrgLogoUrl());
        buyer.setBuyerType(temp.getBuyerType());
        buyer.setGstNumber(temp.getGstNumber());
        buyer.setGstFileUrl(temp.getGstFileUrl());
        buyer.setGstVerified(temp.isGstVerified());
        buyer.setPanNumber(temp.getPanNumber());
        buyer.setPanFileUrl(temp.getPanFileUrl());
        buyer.setPanVerified(temp.isPanVerified());
        buyer.setTermsAccepted(temp.isTermsAccepted());
        buyer.setStatus("APPROVED");
        buyer.setCreatedBy("SYSTEM");
        buyer.setUpdatedBy("SYSTEM");

        Buyer savedBuyer = buyerRepo.save(buyer);

        if (temp.getAddress() != null) {
            BuyerAddress address = new BuyerAddress();
            address.setBuyer(savedBuyer);
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
            savedBuyer.setAddress(address);
        }

        if (temp.getContact() != null) {
            BuyerContact contact = new BuyerContact();
            contact.setBuyer(savedBuyer);
            contact.setName(temp.getContact().getName());
            contact.setDesignation(temp.getContact().getDesignation());
            contact.setEmail(temp.getContact().getEmail());
            contact.setEmailVerified(temp.getContact().isEmailVerified());
            contact.setMobile(temp.getContact().getMobile());
            contact.setPhoneVerified(temp.getContact().isPhoneVerified());
            contact.setCreatedBy("SYSTEM");
            contact.setUpdatedBy("SYSTEM");
            savedBuyer.setContact(contact);
        }

        if (temp.getDocuments() != null && !temp.getDocuments().isEmpty()) {
            for (TempBuyerDocument tempDoc : temp.getDocuments()) {
                BuyerDocument doc = new BuyerDocument();
                doc.setBuyer(savedBuyer);
                doc.setDocumentType(tempDoc.getDocumentType());
                doc.setDocumentNumber(tempDoc.getDocumentNumber());
                doc.setDocumentFileUrl(tempDoc.getDocumentFileUrl());
                doc.setDocumentFileName(tempDoc.getDocumentFileName());
                doc.setDocumentVerified(tempDoc.isDocumentVerified());
                doc.setLicenseIssueDate(tempDoc.getLicenseIssueDate());
                doc.setLicenseExpiryDate(tempDoc.getLicenseExpiryDate());
                doc.setLicenseIssuingAuthority(tempDoc.getLicenseIssuingAuthority());
                doc.setLicenseStatus(tempDoc.getLicenseStatus());
                doc.setCreatedBy("SYSTEM");
                doc.setUpdatedBy("SYSTEM");
                savedBuyer.getDocuments().add(doc);
            }
        }

        Buyer fullyPersistedBuyer = buyerRepo.save(savedBuyer);
        log.info("Phase 1 complete — Buyer persisted with id={}", fullyPersistedBuyer.getBuyerId());
        return fullyPersistedBuyer;
    }

    /**
     * Generates a unique Buyer ID:
     * [2 chars from organization name][buyer type abbreviation][4-digit global sequence]
     */
    private String generateBuyerId(TempBuyer tempBuyer) {
        String namePart = tempBuyer.getOrganizationName()
                .replaceAll("[^A-Za-z0-9]", "")
                .toUpperCase();
        namePart = namePart.length() >= 2 ? namePart.substring(0, 2) : namePart;

        String typePart = tempBuyer.getBuyerType()
                .getBuyerTypeAbbreviation()
                .replaceAll("\\s+", "")
                .toUpperCase();

        buyerRepo.acquireBuyerIdLock();
        Integer nextSequence = buyerRepo.findMaxBuyerSequence() + 1;
        String sequencePart = String.format("%04d", nextSequence);

        return namePart + typePart + sequencePart;
    }

    private void saveReviewHistory(TempBuyer buyer, String status, String comments) {
        TempBuyerReviewHistory history = TempBuyerReviewHistory.builder()
                .tempBuyer(buyer)
                .status(status)
                .comments(comments)
                .reviewedBy("ADMIN")
                .reviewedAt(LocalDateTime.now())
                .build();
        reviewHistoryRepository.save(history);
    }

    /**
     * PHASE 2 (best-effort): Migrates every real S3 file from
     * tempbuyers/{REQ_ID}/... → buyers/{BUYER_ID}/..., updates the DB URL,
     * then deletes the old temp object. Failures are logged, never fatal.
     */
    private void migrateAllBuyerImages(TempBuyer temp, Buyer buyer) {
        String buyerId = buyer.getBuyerId();
        String reqId = temp.getTempBuyerRequestId();

        if (hasUrl(buyer.getOrgLogoUrl())) {
            String newUrl = copyToBuyerFolder(buyer.getOrgLogoUrl(), reqId, buyerId, "orglogo");
            if (newUrl != null) {
                buyer.setOrgLogoUrl(newUrl);
                buyerRepo.save(buyer);
                deleteOldFile(temp.getOrgLogoUrl());
            }
        }

        if (hasUrl(buyer.getGstFileUrl())) {
            String newUrl = copyToBuyerFolder(buyer.getGstFileUrl(), reqId, buyerId, "gst");
            if (newUrl != null) {
                buyer.setGstFileUrl(newUrl);
                buyerRepo.save(buyer);
                deleteOldFile(temp.getGstFileUrl());
            }
        }

        if (hasUrl(buyer.getPanFileUrl())) {
            String newUrl = copyToBuyerFolder(buyer.getPanFileUrl(), reqId, buyerId, "pan");
            if (newUrl != null) {
                buyer.setPanFileUrl(newUrl);
                buyerRepo.save(buyer);
                deleteOldFile(temp.getPanFileUrl());
            }
        }

        if (buyer.getDocuments() != null && !buyer.getDocuments().isEmpty()) {
            for (BuyerDocument doc : buyer.getDocuments()) {
                if (!hasUrl(doc.getDocumentFileUrl())) continue;
                String oldTempUrl = doc.getDocumentFileUrl();
                String newUrl = copyToBuyerFolder(oldTempUrl, reqId, buyerId, "licenses");
                if (newUrl != null) {
                    doc.setDocumentFileUrl(newUrl);
                    deleteOldFile(oldTempUrl);
                }
            }
            buyerRepo.save(buyer);
        }

        log.info("Phase 2 complete — S3 migration finished for buyerId={}", buyerId);
    }

    private String copyToBuyerFolder(String sourceUrl, String reqId, String buyerId, String folder) {
        try {
            String oldKey = s3Service.extractKeyFromUrl(sourceUrl);
            String fileName = oldKey.substring(oldKey.lastIndexOf('/') + 1);
            String newKey = String.format("buyers/%s/%s/%s", buyerId, folder, fileName);
            return s3Service.copyFile(oldKey, newKey);
        } catch (Exception e) {
            log.error("Failed to copy S3 file from url={} to buyers/{}/{}: {}",
                    sourceUrl, buyerId, folder, e.getMessage(), e);
            return null;
        }
    }

    private void deleteOldFile(String url) {
        if (!hasUrl(url)) return;
        try {
            s3Service.deleteFile(s3Service.extractKeyFromUrl(url));
        } catch (Exception e) {
            log.warn("Could not delete old S3 file url={}: {}", url, e.getMessage());
        }
    }

    private boolean hasUrl(String url) {
        return url != null && !url.isBlank() && !"PENDING".equalsIgnoreCase(url.trim());
    }
}
