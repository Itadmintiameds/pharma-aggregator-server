package com.example.pharmaaggregatorserver.service.seller.history;

import com.example.pharmaaggregatorserver.entity.master.ProductTypeMaster;
import com.example.pharmaaggregatorserver.entity.seller.*;
import com.example.pharmaaggregatorserver.entity.seller.history.SellerHistory;
import com.example.pharmaaggregatorserver.repository.auth.UserRepository;
import com.example.pharmaaggregatorserver.repository.seller.history.SellerHistoryRepository;
import com.example.pharmaaggregatorserver.service.EmailService;
import com.example.pharmaaggregatorserver.service.auth.UserCreationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Responsible for two concerns:
 * <p>
 * 1. {@link #snapshotBeforeUpdate} — captures an immutable history row of the
 * current live Seller state BEFORE an approved update overwrites it.
 * <p>
 * 2. {@link #rotateCoordinatorCredentialsIfEmailChanged} — if the coordinator
 * e-mail changed, updates tbl_user (username + temp password) and e-mails
 * the new credentials to the new coordinator address.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SellerHistoryService {

    private final SellerHistoryRepository sellerHistoryRepository;
    private final UserRepository userRepository;
    private final UserCreationService userCreationService;
    private final EmailService emailService;

    private static final String SUPPORT_EMAIL = "support@tiameds.com";

    @Value("${app.frontend-url}")
    private String LOGIN_URL;

    // =========================================================================
    // 1. Snapshot
    // =========================================================================

    /**
     * Reads the current live {@code seller} and writes one immutable row to
     * {@code tbl_seller_history} BEFORE the pending update is applied.
     * <p>
     * Both IDs and human-readable names are frozen for every master-data
     * reference so the history row stays self-describing even if master tables
     * are renamed or re-seeded in the future.
     * <p>
     * S3 files are never touched — old URLs become part of the audit trail.
     *
     * @param seller     the live Seller entity in its PRE-update state
     * @param approvedBy admin username performing the approval
     */
    @Transactional
    public void snapshotBeforeUpdate(Seller seller, String approvedBy) {

        SellerHistory.SellerHistoryBuilder b = SellerHistory.builder()
                .sellerId(seller.getSellerId())
                .approvedBy(approvedBy)

                // ── Core ──────────────────────────────────────────────────────
                .sellerName(seller.getSellerName())
                .sellerImageUrl(seller.getSellerImageUrl())
                .phone(seller.getPhone())
                .email(seller.getEmail())
                .website(seller.getWebsite())
                .status(seller.getStatus())
                .termsAccepted(seller.isTermsAccepted())
                .companyRegistrationCertificateUrl(seller.getCompanyRegistrationCertificateUrl())
                .isCompanyRegistrationCertificateVerified(seller.isCompanyRegistrationCertificateVerified());

        // Company type — freeze both ID and name
        if (seller.getCompanyType() != null) {
            b.companyTypeId(seller.getCompanyType().getCompanyTypeId())
                    .companyTypeName(seller.getCompanyType().getCompanyTypeName());
        }

        // Seller type — freeze both ID and name
        if (seller.getSellerType() != null) {
            b.sellerTypeId(seller.getSellerType().getSellerTypeId())
                    .sellerTypeName(seller.getSellerType().getSellerTypeName());
        }

        // ── Address ───────────────────────────────────────────────────────────
        SellerAddress addr = seller.getAddress();
        if (addr != null) {
            // State — freeze both ID and name
            if (addr.getState() != null) {
                b.stateId(addr.getState().getStateId())
                        .stateName(addr.getState().getStateName());
            }
            // District — freeze both ID and name
            if (addr.getDistrict() != null) {
                b.districtId(addr.getDistrict().getDistrictId())
                        .districtName(addr.getDistrict().getDistrictName());
            }
            // Taluka — freeze both ID and name
            if (addr.getTaluka() != null) {
                b.talukaId(addr.getTaluka().getTalukaId())
                        .talukaName(addr.getTaluka().getTalukaName());
            }
            b.city(addr.getCity())
                    .street(addr.getStreet())
                    .buildingNo(addr.getBuildingNo())
                    .landmark(addr.getLandmark())
                    .pinCode(addr.getPinCode());
        }

        // ── Coordinator ───────────────────────────────────────────────────────
        SellerCoordinator coord = seller.getCoordinator();
        if (coord != null) {
            b.coordinatorName(coord.getName())
                    .coordinatorDesignation(coord.getDesignation())
                    .coordinatorEmail(coord.getEmail())
                    .coordinatorMobile(coord.getMobile());
        }

        // ── Bank details ──────────────────────────────────────────────────────
        SellerBankDetails bank = seller.getBankDetails();
        if (bank != null) {
            b.bankName(bank.getBankName())
                    .bankBranch(bank.getBranch())
                    .bankIfscCode(bank.getIfscCode())
                    .bankAccountNumber(bank.getAccountNumber())
                    .bankAccountHolderName(bank.getAccountHolderName())
                    .bankDocumentFileUrl(bank.getBankDocumentFileUrl())   // old URL preserved
                    .isBankDocumentVerified(bank.isBankDocumentVerified());
        }

        // ── GST ───────────────────────────────────────────────────────────────
        SellerGST gst = seller.getSellerGST();
        if (gst != null) {
            b.gstNumber(gst.getGstNumber())
                    .gstFileUrl(gst.getGstFileUrl())                      // old URL preserved
                    .isGstVerified(gst.isGstVerified());
        }

        // ── Product types — freeze both IDs and names ─────────────────────────
        List<ProductTypeMaster> pts = seller.getProductTypes();
        if (pts != null && !pts.isEmpty()) {
            b.productTypeIds(
                    pts.stream()
                            .map(pt -> String.valueOf(pt.getProductTypeId()))
                            .collect(Collectors.joining(",")));
            b.productTypeNames(
                    pts.stream()
                            .map(ProductTypeMaster::getProductTypeName)
                            .collect(Collectors.joining(",")));
        }

        // ── Documents — freeze as self-contained JSON ─────────────────────────
        List<SellerDocument> docs = seller.getDocuments();
        if (docs != null && !docs.isEmpty()) {
            b.documentsJson(buildDocumentsJson(docs));
        }

        SellerHistory snapshot = sellerHistoryRepository.save(b.build());
        log.info("History snapshot saved: historyId={}, sellerId={}, approvedBy={}",
                snapshot.getHistoryId(), seller.getSellerId(), approvedBy);
    }

    // =========================================================================
    // 2. Coordinator credential rotation
    // =========================================================================

    /**
     * Must be called AFTER the Seller entity has been saved with the new
     * coordinator e-mail already persisted.
     * <p>
     * If the coordinator e-mail has changed:
     * - Delegates to {@link UserCreationService#rotateCoordinatorCredentials}
     * which updates tbl_user: new username, new hashed temp password,
     * isPasswordTemporary = true.
     * - Sends an HTML e-mail to the NEW coordinator address with their
     * username and plain-text temporary password.
     * - On first login the system forces a password change and sets
     * isPasswordTemporary = false.
     * <p>
     * If the e-mail has not changed this method is a no-op.
     *
     * @param seller              fully-updated live Seller (new coordinator email persisted)
     * @param oldCoordinatorEmail coordinator email that was live BEFORE the update
     */
    @Transactional
    public void rotateCoordinatorCredentialsIfEmailChanged(
            Seller seller,
            String oldCoordinatorEmail) {

        String newCoordinatorEmail = (seller.getCoordinator() != null)
                ? seller.getCoordinator().getEmail()
                : null;

        if (newCoordinatorEmail == null
                || newCoordinatorEmail.equalsIgnoreCase(oldCoordinatorEmail)) {
            log.debug("Coordinator email unchanged for sellerId={}. No credential rotation.",
                    seller.getSellerId());
            return;
        }

        log.info("Coordinator email changed for sellerId={}: [{}] → [{}]. Rotating credentials.",
                seller.getSellerId(), oldCoordinatorEmail, newCoordinatorEmail);

        // Delegates username update + password hash to UserCreationService
        UserCreationService.CredentialRotationResult result =
                userCreationService.rotateCoordinatorCredentials(
                        oldCoordinatorEmail, newCoordinatorEmail);

        // Send new credentials to the new coordinator email
        sendCredentialRotationEmail(
                seller.getSellerName(),
                newCoordinatorEmail,
                seller.getCoordinator().getName(),
                result.plainTempPassword()
        );
    }
    //For Profile Update
    @Transactional
    public void rotateCoordinatorCredentialsIfProfileEmailChanged(
            Seller seller,
            String oldCoordinatorEmail) {

        String newCoordinatorEmail = (seller.getCoordinator() != null)
                ? seller.getCoordinator().getEmail()
                : null;

        if (newCoordinatorEmail == null
                || newCoordinatorEmail.equalsIgnoreCase(oldCoordinatorEmail)) {
            log.debug("Coordinator email unchanged for sellerId={}. No credential rotation.",
                    seller.getSellerId());
            return;
        }

        log.info("Coordinator email changed for sellerId={}: [{}] → [{}]. Rotating credentials.",
                seller.getSellerId(), oldCoordinatorEmail, newCoordinatorEmail);

        // Delegates username update + password hash to UserCreationService
        UserCreationService.CredentialRotationResult result =
                userCreationService.rotateCoordinatorCredentials(
                        oldCoordinatorEmail, newCoordinatorEmail);

        // Send new credentials to the new coordinator email
//        sendCredentialRotationEmail(
//                seller.getSellerName(),
//                newCoordinatorEmail,
//                seller.getCoordinator().getName(),
//                result.plainTempPassword()
//        );
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    /**
     * Builds a JSON array string that captures each document fully, including
     * the productTypeName frozen at snapshot time, so no joins are ever needed
     * to read the history.
     */
    private String buildDocumentsJson(List<SellerDocument> docs) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < docs.size(); i++) {
            SellerDocument d = docs.get(i);
            Long ptId = (d.getProductTypes() != null) ? d.getProductTypes().getProductTypeId() : null;
            String ptName = (d.getProductTypes() != null) ? d.getProductTypes().getProductTypeName() : "";

            sb.append("{")
                    .append("\"sellerDocumentId\":").append(d.getSellerDocumentsId()).append(",")
                    .append("\"productTypeId\":").append(ptId).append(",")
                    .append("\"productTypeName\":\"").append(esc(ptName)).append("\",")
                    .append("\"documentNumber\":\"").append(esc(d.getDocumentNumber())).append("\",")
                    .append("\"documentFileUrl\":\"").append(esc(d.getDocumentFileUrl())).append("\",")
                    .append("\"licenseIssueDate\":\"").append(d.getLicenseIssueDate()).append("\",")
                    .append("\"licenseExpiryDate\":\"").append(d.getLicenseExpiryDate()).append("\",")
                    .append("\"licenseIssuingAuthority\":\"").append(esc(d.getLicenseIssuingAuthority())).append("\",")
                    .append("\"licenseStatus\":\"").append(esc(d.getLicenseStatus())).append("\",")
                    .append("\"isDocumentVerified\":").append(d.isDocumentVerified())
                    .append("}");
            if (i < docs.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Minimal JSON string escaping — handles backslash and double-quote.
     */
    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private void sendCredentialRotationEmail(
            String sellerName,
            String toEmail,
            String coordinatorName,
            String plainTempPassword) {

        String body = """
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                
                <p>Dear %s,</p>
                
                <p>
                    You have been registered as the new coordinator for <b>%s</b> on the
                    <b>TiaMeds Marketplace platform</b>.
                </p>
                
                <p>Your login credentials have been updated. Please use the details below:</p>
                
                <table style="border-collapse:collapse;">
                  <tr>
                    <td style="padding: 4px 12px 4px 0;"><b>Username (Login E-mail):</b></td>
                    <td>%s</td>
                  </tr>
                  <tr>
                    <td style="padding: 4px 12px 4px 0;"><b>Temporary Password:</b></td>
                    <td>%s</td>
                  </tr>
                </table>
                
                <p>
                    For security purposes you will be prompted to set a new password upon
                    your first login.
                </p>
                
                <p>
                    Platform Access:
                    <a href="%s" style="color:#1a73e8; text-decoration:none;">%s</a>
                </p>
                
                <p>
                    If you have any questions please contact us at
                    <a href="mailto:%s">%s</a>.
                </p>
                
                <p>
                    Warm Regards,<br>
                    TiaMeds Marketplace<br>
                    Seller Onboarding &amp; Compliance Team
                </p>
                
                </body>
                </html>
                """.formatted(
                coordinatorName,
                sellerName,
                toEmail,
                plainTempPassword,
                LOGIN_URL, LOGIN_URL,
                SUPPORT_EMAIL, SUPPORT_EMAIL
        );

        emailService.sendHtmlMail(
                toEmail,
                "Your TiaMeds Marketplace Login Credentials Have Been Updated",
                body
        );

        log.info("Credential rotation e-mail sent to '{}'", toEmail);
    }
}