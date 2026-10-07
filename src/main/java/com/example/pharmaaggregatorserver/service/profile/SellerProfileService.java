package com.example.pharmaaggregatorserver.service.profile;

import com.example.pharmaaggregatorserver.dto.seller.profile.*;
import com.example.pharmaaggregatorserver.entity.master.CompanyTypeMaster;
import com.example.pharmaaggregatorserver.entity.master.DistrictMaster;
import com.example.pharmaaggregatorserver.entity.master.ProductTypeMaster;
import com.example.pharmaaggregatorserver.entity.master.SellerTypeMaster;
import com.example.pharmaaggregatorserver.entity.master.StateMaster;
import com.example.pharmaaggregatorserver.entity.master.TalukaMaster;
import com.example.pharmaaggregatorserver.entity.seller.*;
import com.example.pharmaaggregatorserver.entity.seller.profile.PendingSeller;
import com.example.pharmaaggregatorserver.entity.seller.profile.PendingSellerDocument;
import com.example.pharmaaggregatorserver.exception.ResourceNotFoundException;
import com.example.pharmaaggregatorserver.exception.DuplicateRequestException;
import com.example.pharmaaggregatorserver.mapper.seller.profile.SellerByIdMapper;
import com.example.pharmaaggregatorserver.repository.master.*;
import com.example.pharmaaggregatorserver.repository.seller.SellerRepository;
import com.example.pharmaaggregatorserver.repository.seller.profile.PendingSellerDocumentRepository;
import com.example.pharmaaggregatorserver.repository.seller.profile.PendingSellerRepository;
import com.example.pharmaaggregatorserver.service.EmailService;
import com.example.pharmaaggregatorserver.service.S3Service;
import com.example.pharmaaggregatorserver.service.seller.history.SellerHistoryService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerProfileService {
    private final SellerByIdMapper sellerByIdMapper;
    private final SellerRepository sellerRepository;
    private final PendingSellerRepository pendingSellerRepository;
    private final PendingSellerDocumentRepository pendingSellerDocumentRepository;
    private final CompanyTypeMasterRepository companyTypeRepository;
    private final SellerTypeMasterRepository sellerTypeRepository;
    private final StateMasterRepository stateRepository;
    private final DistrictMasterRepository districtRepository;
    private final TalukaMasterRepository talukaRepository;
    private final ProductTypeMasterRepository productTypeRepository;
    private final EmailService emailService;
    private final CoordinatorEmailService coordinatorEmailService;
    private final S3Service s3Service;
    private final SellerHistoryService sellerHistoryService;

    @Value("${app.support.email:support@tiameds.com}")
    private String supportEmail;

    @Value("${app.login.url:https://marketplace.tiameds.com/login}")
    private String loginUrl;

    @PersistenceContext
    private EntityManager entityManager;

    private static final DateTimeFormatter TS_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Transactional
    public SellerResponseDTO requestSellerUpdate(String sellerId, SellerEditRequest request, String requestedBy) {
        try {
            log.info("Starting requestSellerUpdate for sellerId: {}, requestedBy: {}", sellerId, requestedBy);
            log.debug("Request payload: {}", request);

            // Fetch existing seller to validate it exists and check for changes
            Seller existingSeller = sellerRepository.findById(sellerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Seller not found with id: " + sellerId));

            log.info("Found existing seller: {}", existingSeller.getSellerId());

            // Check if there's already a pending request for this seller
            List<PendingSeller> pendingRequests = pendingSellerRepository.findBySellerIdAndStatus(sellerId, "PENDING");
            if (!pendingRequests.isEmpty()) {
                log.warn("Pending request already exists for sellerId: {}", sellerId);
                // Throw custom exception with existing pending request ID
                Long existingPendingId = pendingRequests.get(0).getPendingSellerId();
                throw new DuplicateRequestException("A pending update request already exists for this seller. Pending Request ID: " + existingPendingId);
            }

            // Determine if admin approval is required based on the changes
            boolean requiresAdminApproval = requiresAdminApproval(existingSeller, request);
            log.info("Requires admin approval: {}", requiresAdminApproval);

            // ALWAYS create a pending seller record for every request
            PendingSeller pendingSeller = createPendingSellerFromRequest(request, requestedBy);
            pendingSeller.setSellerId(sellerId);
            pendingSeller.setRequestType("UPDATE");

            if (!requiresAdminApproval) {
                // For auto-approval, set status to AUTO_APPROVED
                pendingSeller.setStatus("AUTO_APPROVED");
                pendingSeller.setApprovedBy("SYSTEM");
                pendingSeller.setApprovedAt(LocalDateTime.now());
            } else {
                // For admin approval, set status to PENDING
                pendingSeller.setStatus("PENDING");
            }

            log.info("Saving pending seller...");
            PendingSeller savedPending = pendingSellerRepository.save(pendingSeller);
            log.info("Saved pending seller with ID: {}", savedPending.getPendingSellerId());

            // Save documents separately
            if (request.getDocuments() != null && !request.getDocuments().isEmpty()) {
                log.info("Saving {} documents...", request.getDocuments().size());
                saveDocuments(savedPending, request.getDocuments());
            }

            // Refresh to load documents
            savedPending = pendingSellerRepository.findById(savedPending.getPendingSellerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Pending seller not found"));

            // Send acknowledgement email for PENDING requests (admin approval required)
            if (requiresAdminApproval) {
                sendUpdateSubmissionAcknowledgementEmail(savedPending, existingSeller.getSellerName());
            }

            SellerResponseDTO response;

            if (!requiresAdminApproval) {
                log.info("Processing auto-approval for pending seller ID: {}", savedPending.getPendingSellerId());
                // Auto-approve - update seller directly
                response = autoApproveUpdate(existingSeller, request, requestedBy, savedPending);
                // Make sure pendingSellerId is set in response
                response.setPendingSellerId(savedPending.getPendingSellerId());
                response.setMessage("Your changes have been auto-approved and applied successfully");
            } else {
                log.info("Returning pending approval response for pending seller ID: {}", savedPending.getPendingSellerId());
                // Return response indicating pending approval
                response = mapToResponseDTO(savedPending);
                response.setPendingSellerId(savedPending.getPendingSellerId());
                response.setMessage("Update request submitted successfully and pending admin approval");
            }

            // Also set the documents list in response with pending IDs
            if (savedPending.getDocuments() != null && !savedPending.getDocuments().isEmpty()) {
                log.info("Setting {} documents in response", savedPending.getDocuments().size());
                List<SellerResponseDTO.DocumentDTO> documentDTOs = savedPending.getDocuments().stream()
                        .map(doc -> {
                            SellerResponseDTO.DocumentDTO dto = new SellerResponseDTO.DocumentDTO();
                            dto.setPendingSellerDocumentId(doc.getId());
                            dto.setProductTypeId(doc.getProductType().getProductTypeId());
                            dto.setProductTypeName(doc.getProductType().getProductTypeName());
                            dto.setDocumentNumber(doc.getDocumentNumber());
                            dto.setDocumentFileUrl(doc.getDocumentFileUrl());
                            dto.setLicenseIssueDate(doc.getLicenseIssueDate());
                            dto.setLicenseExpiryDate(doc.getLicenseExpiryDate());
                            dto.setLicenseIssuingAuthority(doc.getLicenseIssuingAuthority());
                            return dto;
                        })
                        .collect(Collectors.toList());
                response.setDocuments(documentDTOs);
            }

            log.info("Successfully completed requestSellerUpdate for sellerId: {}", sellerId);
            return response;

        } catch (DuplicateRequestException e) {
            log.error("Duplicate request for sellerId: {}", sellerId, e);
            throw e;
        } catch (Exception e) {
            log.error("Error in requestSellerUpdate for sellerId: {}", sellerId, e);
            throw new RuntimeException("Failed to process seller update request: " + e.getMessage(), e);
        }
    }

    @Transactional
    public PendingSeller createPendingSeller(SellerEditRequest request, String requestedBy) {
        PendingSeller pendingSeller = createPendingSellerFromRequest(request, requestedBy);
        pendingSeller.setRequestType("CREATE");
        pendingSeller.setStatus("PENDING");
        PendingSeller savedPending = pendingSellerRepository.save(pendingSeller);

        // Save documents separately
        if (request.getDocuments() != null && !request.getDocuments().isEmpty()) {
            saveDocuments(savedPending, request.getDocuments());
        }

        return savedPending;
    }

    /**
     * Determines if admin approval is required based on which fields were updated
     */
    private boolean requiresAdminApproval(Seller existingSeller, SellerEditRequest request) {
        // Company Details - Admin approval required for:
        // 1. Seller Name change (requires updated registration certificate)
        if (request.getSellerName() != null && !request.getSellerName().equals(existingSeller.getSellerName())) {
            log.info("Admin approval required: Seller name changed from '{}' to '{}'",
                    existingSeller.getSellerName(), request.getSellerName());
            return true;
        }

        // 2. Address change (requires updated registration certificate with new address)
        if (isAddressChanged(existingSeller, request)) {
            log.info("Admin approval required: Address changed");
            return true;
        }

        // Statutory Documents - Admin approval required for:
        // 1. License details change (documents updated)
        if (areLicenseDetailsChanged(existingSeller, request)) {
            log.info("Admin approval required: License details changed");
            return true;
        }

        // 2. GST Number change (requires updated GST certificate)
        String existingGstNumber = existingSeller.getSellerGST() != null ?
                existingSeller.getSellerGST().getGstNumber() : null;
        if (request.getGstNumber() != null && !request.getGstNumber().equals(existingGstNumber)) {
            log.info("Admin approval required: GST number changed from '{}' to '{}'",
                    existingGstNumber, request.getGstNumber());
            return true;
        }

        // 3. GST Document change (requires admin approval for new document)
        String existingGstFileUrl = existingSeller.getSellerGST() != null ?
                existingSeller.getSellerGST().getGstFileUrl() : null;
        if (request.getGstFileUrl() != null && !request.getGstFileUrl().equals(existingGstFileUrl)) {
            log.info("Admin approval required: GST document changed");
            return true;
        }

        // 4. Company Registration Certificate change (requires admin approval)
        String existingCompanyRegUrl = existingSeller.getCompanyRegistrationCertificateUrl();
        if (request.getCompanyRegistrationCertificateUrl() != null &&
                !request.getCompanyRegistrationCertificateUrl().equals(existingCompanyRegUrl)) {
            log.info("Admin approval required: Company Registration Certificate changed");
            return true;
        }

        // Bank Account Details - Admin approval required for any bank info change
        if (areBankDetailsChanged(existingSeller, request)) {
            log.info("Admin approval required: Bank details changed");
            return true;
        }

        // No admin approval required for:
        // - Seller Type (frozen - should not be updated)
        // - Company Type
        // - Phone Number
        // - Email ID
        // - Website
        // - Coordinator Name, Designation, Email, Mobile
        return false;
    }

    /**
     * Checks if address fields have changed
     */
    private boolean isAddressChanged(Seller existingSeller, SellerEditRequest request) {
        if (request.getAddress() == null) {
            return false;
        }

        SellerAddress existingAddress = existingSeller.getAddress();
        if (existingAddress == null && request.getAddress() != null) {
            return true;
        }

        SellerEditRequest.AddressDTO newAddress = request.getAddress();

        Long existingStateId = existingAddress.getState() != null ? existingAddress.getState().getStateId() : null;
        Long existingDistrictId = existingAddress.getDistrict() != null ? existingAddress.getDistrict().getDistrictId() : null;
        Long existingTalukaId = existingAddress.getTaluka() != null ? existingAddress.getTaluka().getTalukaId() : null;

        return !equalsOrBothNull(existingStateId, newAddress.getStateId()) ||
                !equalsOrBothNull(existingDistrictId, newAddress.getDistrictId()) ||
                !equalsOrBothNull(existingTalukaId, newAddress.getTalukaId()) ||
                !equalsOrBothNull(existingAddress.getCity(), newAddress.getCity()) ||
                !equalsOrBothNull(existingAddress.getStreet(), newAddress.getStreet()) ||
                !equalsOrBothNull(existingAddress.getBuildingNo(), newAddress.getBuildingNo()) ||
                !equalsOrBothNull(existingAddress.getLandmark(), newAddress.getLandmark()) ||
                !equalsOrBothNull(existingAddress.getPinCode(), newAddress.getPinCode());
    }

    /**
     * Checks if license details have changed (documents)
     */
    private boolean areLicenseDetailsChanged(Seller existingSeller, SellerEditRequest request) {
        if (request.getDocuments() == null || request.getDocuments().isEmpty()) {
            return false;
        }

        List<SellerDocument> existingDocuments = existingSeller.getDocuments();

        // Check if number of documents changed
        if (existingDocuments.size() != request.getDocuments().size()) {
            return true;
        }

        // Check each document for changes
        for (int i = 0; i < existingDocuments.size(); i++) {
            SellerDocument existingDoc = existingDocuments.get(i);
            SellerEditRequest.DocumentDTO newDoc = request.getDocuments().get(i);

            Long existingProductTypeId = existingDoc.getProductTypes() != null ?
                    existingDoc.getProductTypes().getProductTypeId() : null;

            if (!equalsOrBothNull(existingProductTypeId, newDoc.getProductTypeId())) {
                return true;
            }

            if (!equalsOrBothNull(existingDoc.getDocumentNumber(), newDoc.getDocumentNumber())) {
                return true;
            }

            if (!equalsOrBothNull(existingDoc.getDocumentFileUrl(), newDoc.getDocumentFileUrl())) {
                return true;
            }

            if (!equalsDateOrBothNull(existingDoc.getLicenseIssueDate(), newDoc.getLicenseIssueDate())) {
                return true;
            }

            if (!equalsDateOrBothNull(existingDoc.getLicenseExpiryDate(), newDoc.getLicenseExpiryDate())) {
                return true;
            }

            if (!equalsOrBothNull(existingDoc.getLicenseIssuingAuthority(), newDoc.getLicenseIssuingAuthority())) {
                return true;
            }

            if (!equalsOrBothNull(existingDoc.getLicenseStatus(), newDoc.getLicenseStatus())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if bank details have changed (any change requires admin approval)
     */
    private boolean areBankDetailsChanged(Seller existingSeller, SellerEditRequest request) {
        if (request.getBankDetails() == null) {
            return false;
        }

        SellerBankDetails existingBank = existingSeller.getBankDetails();
        if (existingBank == null && request.getBankDetails() != null) {
            return true;
        }

        SellerEditRequest.BankDetailsDTO newBank = request.getBankDetails();

        return !equalsOrBothNull(existingBank.getBankName(), newBank.getBankName()) ||
                !equalsOrBothNull(existingBank.getBranch(), newBank.getBranch()) ||
                !equalsOrBothNull(existingBank.getIfscCode(), newBank.getIfscCode()) ||
                !equalsOrBothNull(existingBank.getAccountNumber(), newBank.getAccountNumber()) ||
                !equalsOrBothNull(existingBank.getAccountHolderName(), newBank.getAccountHolderName()) ||
                !equalsOrBothNull(existingBank.getBankDocumentFileUrl(), newBank.getBankDocumentFileUrl());
    }

    /**
     * Auto-approve update - directly update seller without admin approval
     */
    @Transactional
    private SellerResponseDTO autoApproveUpdate(Seller seller, SellerEditRequest request, String requestedBy, PendingSeller pendingRecord) {
        // Capture old coordinator before any changes
        SellerCoordinator oldCoordinator = null;
        if (seller.getCoordinator() != null) {
            oldCoordinator = new SellerCoordinator();
            oldCoordinator.setName(seller.getCoordinator().getName());
            oldCoordinator.setDesignation(seller.getCoordinator().getDesignation());
            oldCoordinator.setEmail(seller.getCoordinator().getEmail());
            oldCoordinator.setMobile(seller.getCoordinator().getMobile());
        }

        // Capture old coordinator email for rotation
        String oldCoordinatorEmail = (seller.getCoordinator() != null)
                ? seller.getCoordinator().getEmail()
                : null;

        // Snapshot before update for history
        sellerHistoryService.snapshotBeforeUpdate(seller, requestedBy);

        // Update fields using EntityManager partial updates
        boolean hasUpdates = false;
        boolean coordinatorEmailChanged = false;

        // Update coordinator if provided (auto-approved)
        if (request.getCoordinator() != null) {
            coordinatorEmailChanged = updateCoordinatorOnly(seller.getSellerId(), request.getCoordinator(), oldCoordinatorEmail);
            hasUpdates = true;
        }

        // Update company type if provided (auto-approved)
        if (request.getCompanyTypeId() != null) {
            updateCompanyTypeOnly(seller.getSellerId(), request.getCompanyTypeId());
            hasUpdates = true;
        }

        // Update phone if provided (auto-approved)
        if (request.getPhone() != null && !request.getPhone().equals(seller.getPhone())) {
            updatePhoneOnly(seller.getSellerId(), request.getPhone());
            hasUpdates = true;
        }

        // Update email if provided (auto-approved)
        if (request.getEmail() != null && !request.getEmail().equals(seller.getEmail())) {
            updateEmailOnly(seller.getSellerId(), request.getEmail());
            hasUpdates = true;
        }

        // Update website if provided (auto-approved)
        if (request.getWebsite() != null && !request.getWebsite().equals(seller.getWebsite())) {
            updateWebsiteOnly(seller.getSellerId(), request.getWebsite());
            hasUpdates = true;
        }

        // Update terms accepted if provided
        if (request.getTermsAccepted() != null && request.getTermsAccepted() != seller.isTermsAccepted()) {
            updateTermsAcceptedOnly(seller.getSellerId(), request.getTermsAccepted());
            hasUpdates = true;
        }

        // Update timestamp if any updates were made
        if (hasUpdates) {
            updateTimestampOnly(seller.getSellerId());
        }

        // Refresh the seller entity to get updated values
        entityManager.flush();
        entityManager.clear();
        Seller updatedSeller = sellerRepository.findById(seller.getSellerId())
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found after update"));

        // Send coordinator email notifications if coordinator details changed
        if (request.getCoordinator() != null && oldCoordinator != null) {
            String temporaryPassword = null;
            if (coordinatorEmailChanged) {
                temporaryPassword = generateTemporaryPassword();
            }
            sendCoordinatorEmailNotificationsWithOldCoordinator(updatedSeller, oldCoordinator, temporaryPassword);
        }

        // Rotate credentials if coordinator email changed
        if (coordinatorEmailChanged) {
            sellerHistoryService.rotateCoordinatorCredentialsIfProfileEmailChanged(updatedSeller, oldCoordinatorEmail);
        }

        // Send notification email about auto-approval
        sendAutoApprovalEmail(updatedSeller, requestedBy);

        log.info("Auto-approved update for seller ID: {} by: {}", updatedSeller.getSellerId(), requestedBy);

        // Return response indicating auto-approval
        SellerResponseDTO response = sellerByIdMapper.toResponseDTO(updatedSeller);
        response.setPendingSellerId(pendingRecord.getPendingSellerId());
        response.setMessage("Your changes have been auto-approved and applied successfully");

        // Also set documents from pending record if needed
        if (pendingRecord.getDocuments() != null && !pendingRecord.getDocuments().isEmpty()) {
            List<SellerResponseDTO.DocumentDTO> documentDTOs = pendingRecord.getDocuments().stream()
                    .map(doc -> {
                        SellerResponseDTO.DocumentDTO dto = new SellerResponseDTO.DocumentDTO();
                        dto.setPendingSellerDocumentId(doc.getId());
                        dto.setProductTypeId(doc.getProductType().getProductTypeId());
                        dto.setProductTypeName(doc.getProductType().getProductTypeName());
                        dto.setDocumentNumber(doc.getDocumentNumber());
                        dto.setDocumentFileUrl(doc.getDocumentFileUrl());
                        dto.setLicenseIssueDate(doc.getLicenseIssueDate());
                        dto.setLicenseExpiryDate(doc.getLicenseExpiryDate());
                        dto.setLicenseIssuingAuthority(doc.getLicenseIssuingAuthority());
                        return dto;
                    })
                    .collect(Collectors.toList());
            response.setDocuments(documentDTOs);
        }

        return response;
    }

    private boolean updateCoordinatorOnly(String sellerId, SellerEditRequest.CoordinatorDTO coord, String oldEmail) {
        boolean emailChanged = false;

        // First check if coordinator exists
        String findCoordinatorIdQuery = "SELECT s.coordinator.id FROM Seller s WHERE s.sellerId = :sellerId";
        Long coordinatorId = null;
        try {
            coordinatorId = (Long) entityManager.createQuery(findCoordinatorIdQuery)
                    .setParameter("sellerId", sellerId)
                    .getSingleResult();
        } catch (Exception e) {
            // Coordinator doesn't exist yet
        }

        LocalDateTime now = LocalDateTime.now();

        if (coordinatorId == null) {
            // Create new coordinator
            String createCoordinatorQuery = "INSERT INTO tbl_seller_coordinator " +
                    "(seller_id, name, designation, email, mobile, created_by, created_at, updated_by, updated_at) " +
                    "VALUES (:sellerId, :name, :designation, :email, :mobile, :createdBy, :createdAt, :updatedBy, :updatedAt)";

            entityManager.createNativeQuery(createCoordinatorQuery)
                    .setParameter("sellerId", sellerId)
                    .setParameter("name", coord.getName())
                    .setParameter("designation", coord.getDesignation())
                    .setParameter("email", coord.getEmail())
                    .setParameter("mobile", coord.getMobile())
                    .setParameter("createdBy", "system")
                    .setParameter("createdAt", now)
                    .setParameter("updatedBy", "system")
                    .setParameter("updatedAt", now)
                    .executeUpdate();

            emailChanged = (oldEmail != null && !oldEmail.equals(coord.getEmail()));
        } else {
            // Check if email is changing
            String findCurrentEmail = "SELECT s.coordinator.email FROM Seller s WHERE s.sellerId = :sellerId";
            String currentEmail = null;
            try {
                currentEmail = (String) entityManager.createQuery(findCurrentEmail)
                        .setParameter("sellerId", sellerId)
                        .getSingleResult();
            } catch (Exception e) {
                // Ignore
            }
            emailChanged = (currentEmail != null && !currentEmail.equals(coord.getEmail()));

            // Update existing coordinator
            StringBuilder updateQuery = new StringBuilder("UPDATE tbl_seller_coordinator SET ");
            boolean first = true;

            if (coord.getName() != null) {
                updateQuery.append("name = :name");
                first = false;
            }
            if (coord.getDesignation() != null) {
                if (!first) updateQuery.append(", ");
                updateQuery.append("designation = :designation");
                first = false;
            }
            if (coord.getEmail() != null) {
                if (!first) updateQuery.append(", ");
                updateQuery.append("email = :email");
                first = false;
            }
            if (coord.getMobile() != null) {
                if (!first) updateQuery.append(", ");
                updateQuery.append("mobile = :mobile");
                first = false;
            }
            updateQuery.append(", updated_by = :updatedBy, updated_at = :updatedAt");
            updateQuery.append(" WHERE seller_id = :sellerId");

            var query = entityManager.createNativeQuery(updateQuery.toString());
            query.setParameter("sellerId", sellerId);
            query.setParameter("updatedBy", "system");
            query.setParameter("updatedAt", now);

            if (coord.getName() != null) query.setParameter("name", coord.getName());
            if (coord.getDesignation() != null) query.setParameter("designation", coord.getDesignation());
            if (coord.getEmail() != null) query.setParameter("email", coord.getEmail());
            if (coord.getMobile() != null) query.setParameter("mobile", coord.getMobile());

            query.executeUpdate();
        }

        return emailChanged;
    }

    private void sendCoordinatorEmailNotificationsWithOldCoordinator(Seller updatedSeller, SellerCoordinator oldCoordinator, String temporaryPassword) {
        SellerCoordinator newCoordinator = updatedSeller.getCoordinator();

        if (newCoordinator == null) return;

        boolean emailChanged = !oldCoordinator.getEmail().equals(newCoordinator.getEmail());
        boolean nameChanged = !oldCoordinator.getName().equals(newCoordinator.getName());
        boolean designationChanged = !oldCoordinator.getDesignation().equals(newCoordinator.getDesignation());
        boolean mobileChanged = !oldCoordinator.getMobile().equals(newCoordinator.getMobile());

        // If nothing changed, don't send any emails
        if (!nameChanged && !designationChanged && !mobileChanged && !emailChanged) {
            return;
        }

        CoordinatorEmailDTO.CoordinatorEmailDTOBuilder builder = CoordinatorEmailDTO.builder()
                .coordinatorName(newCoordinator.getName())
                .sellerCompanyName(updatedSeller.getSellerName())
                .oldName(oldCoordinator.getName())
                .newName(newCoordinator.getName())
                .oldDesignation(oldCoordinator.getDesignation())
                .newDesignation(newCoordinator.getDesignation())
                .oldMobile(oldCoordinator.getMobile())
                .newMobile(newCoordinator.getMobile())
                .oldEmail(oldCoordinator.getEmail())
                .newEmail(newCoordinator.getEmail())
                .supportEmail(supportEmail)
                .loginUrl(loginUrl);

        if (emailChanged) {
            // Send security alert to OLD email
            CoordinatorEmailDTO alertDto = builder
                    .temporaryPassword(temporaryPassword)
                    .build();
            coordinatorEmailService.sendCoordinatorEmailChangeSecurityAlert(alertDto);

            // Send update notification to NEW email with credentials
            CoordinatorEmailDTO updateDto = builder
                    .temporaryPassword(temporaryPassword)
                    .build();
            coordinatorEmailService.sendCoordinatorDetailsUpdateWithEmailChange(updateDto);
        } else {
            // Email not changed - send update notification to existing email
            CoordinatorEmailDTO updateDto = builder.build();
            coordinatorEmailService.sendCoordinatorDetailsUpdateWithoutEmailChange(updateDto);
        }
    }

    private String generateTemporaryPassword() {
        // Generate a random temporary password
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }

    private void updateCompanyTypeOnly(String sellerId, Long companyTypeId) {
        String query = "UPDATE tbl_seller SET company_type_id = :companyTypeId WHERE seller_id = :sellerId";
        entityManager.createNativeQuery(query)
                .setParameter("companyTypeId", companyTypeId)
                .setParameter("sellerId", sellerId)
                .executeUpdate();
    }

    private void updatePhoneOnly(String sellerId, String phone) {
        String query = "UPDATE tbl_seller SET phone = :phone WHERE seller_id = :sellerId";
        entityManager.createNativeQuery(query)
                .setParameter("phone", phone)
                .setParameter("sellerId", sellerId)
                .executeUpdate();
    }

    private void updateEmailOnly(String sellerId, String email) {
        String query = "UPDATE tbl_seller SET email = :email WHERE seller_id = :sellerId";
        entityManager.createNativeQuery(query)
                .setParameter("email", email)
                .setParameter("sellerId", sellerId)
                .executeUpdate();
    }

    private void updateWebsiteOnly(String sellerId, String website) {
        String query = "UPDATE tbl_seller SET website = :website WHERE seller_id = :sellerId";
        entityManager.createNativeQuery(query)
                .setParameter("website", website)
                .setParameter("sellerId", sellerId)
                .executeUpdate();
    }

    private void updateTermsAcceptedOnly(String sellerId, Boolean termsAccepted) {
        String query = "UPDATE tbl_seller SET terms_accepted = :termsAccepted WHERE seller_id = :sellerId";
        entityManager.createNativeQuery(query)
                .setParameter("termsAccepted", termsAccepted)
                .setParameter("sellerId", sellerId)
                .executeUpdate();
    }

    private void updateTimestampOnly(String sellerId) {
        String query = "UPDATE tbl_seller SET updated_at = :updatedAt, updated_by = :updatedBy WHERE seller_id = :sellerId";
        entityManager.createNativeQuery(query)
                .setParameter("updatedAt", LocalDateTime.now())
                .setParameter("updatedBy", "system")
                .setParameter("sellerId", sellerId)
                .executeUpdate();
    }

    /**
     * Send auto-approval notification email
     */
    private void sendAutoApprovalEmail(Seller seller, String approvedBy) {
        String coordinatorEmail = seller.getCoordinator() != null ? seller.getCoordinator().getEmail() : null;
        String coordinatorName = seller.getCoordinator() != null ? seller.getCoordinator().getName() : "Coordinator";

        if (coordinatorEmail == null || coordinatorEmail.isEmpty()) {
            log.warn("No coordinator email found for seller ID: {}", seller.getSellerId());
            return;
        }

        coordinatorEmailService.sendAutoApprovalEmail(
                coordinatorEmail,
                coordinatorName,
                seller.getSellerId(),
                seller.getSellerName(),
                approvedBy
        );
    }

    private boolean equalsOrBothNull(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    private boolean equalsDateOrBothNull(LocalDate a, LocalDate b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    private void saveDocuments(PendingSeller pendingSeller, List<SellerEditRequest.DocumentDTO> documentDTOs) {
        for (SellerEditRequest.DocumentDTO docDTO : documentDTOs) {
            PendingSellerDocument document = new PendingSellerDocument();
            document.setPendingSeller(pendingSeller);

            ProductTypeMaster productType = productTypeRepository.findById(docDTO.getProductTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product type not found with id: " + docDTO.getProductTypeId()));
            document.setProductType(productType);

            document.setDocumentNumber(docDTO.getDocumentNumber());
            document.setDocumentFileUrl(docDTO.getDocumentFileUrl());
            document.setLicenseIssueDate(docDTO.getLicenseIssueDate());
            document.setLicenseExpiryDate(docDTO.getLicenseExpiryDate());
            document.setLicenseIssuingAuthority(docDTO.getLicenseIssuingAuthority());
            document.setLicenseStatus(docDTO.getLicenseStatus());

            pendingSellerDocumentRepository.save(document);
            pendingSeller.getDocuments().add(document);
        }
    }

    private PendingSeller createPendingSellerFromRequest(SellerEditRequest request, String requestedBy) {
        log.info("Creating pending seller from request");

        // Validate required fields
        if (request.getSellerName() == null || request.getSellerName().trim().isEmpty()) {
            throw new IllegalArgumentException("Seller name is required");
        }
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number is required");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (request.getCompanyTypeId() == null) {
            throw new IllegalArgumentException("Company type is required");
        }
        if (request.getSellerTypeId() == null) {
            throw new IllegalArgumentException("Seller type is required");
        }
        if (request.getProductTypeId() == null || request.getProductTypeId().isEmpty()) {
            throw new IllegalArgumentException("At least one product type is required");
        }
        if (request.getAddress() == null) {
            throw new IllegalArgumentException("Address is required");
        }
        if (request.getCoordinator() == null) {
            throw new IllegalArgumentException("Coordinator details are required");
        }
        if (request.getBankDetails() == null) {
            throw new IllegalArgumentException("Bank details are required");
        }

        PendingSeller pendingSeller = new PendingSeller();

        // Basic info
        pendingSeller.setSellerName(request.getSellerName());
        pendingSeller.setPhone(request.getPhone());
        pendingSeller.setEmail(request.getEmail());
        pendingSeller.setWebsite(request.getWebsite());
        pendingSeller.setTermsAccepted(request.getTermsAccepted() != null ? request.getTermsAccepted() : false);

        // Company and seller types
        if (request.getCompanyTypeId() != null) {
            CompanyTypeMaster companyType = companyTypeRepository.findById(request.getCompanyTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Company type not found with id: " + request.getCompanyTypeId()));
            pendingSeller.setCompanyType(companyType);
        }

        if (request.getSellerTypeId() != null) {
            SellerTypeMaster sellerType = sellerTypeRepository.findById(request.getSellerTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Seller type not found with id: " + request.getSellerTypeId()));
            pendingSeller.setSellerType(sellerType);
        }

        // Address fields
        if (request.getAddress() != null) {
            SellerEditRequest.AddressDTO addr = request.getAddress();

            log.info("Processing address: stateId={}, districtId={}, talukaId={}",
                    addr.getStateId(), addr.getDistrictId(), addr.getTalukaId());

            StateMaster state = stateRepository.findById(addr.getStateId())
                    .orElseThrow(() -> new ResourceNotFoundException("State not found with id: " + addr.getStateId()));
            DistrictMaster district = districtRepository.findById(addr.getDistrictId())
                    .orElseThrow(() -> new ResourceNotFoundException("District not found with id: " + addr.getDistrictId()));
            TalukaMaster taluka = talukaRepository.findById(addr.getTalukaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Taluka not found with id: " + addr.getTalukaId()));

            pendingSeller.setState(state);
            pendingSeller.setDistrict(district);
            pendingSeller.setTaluka(taluka);
            pendingSeller.setCity(addr.getCity());
            pendingSeller.setStreet(addr.getStreet());
            pendingSeller.setBuildingNo(addr.getBuildingNo());
            pendingSeller.setLandmark(addr.getLandmark());
            pendingSeller.setPinCode(addr.getPinCode());
        }

        // Coordinator fields
        if (request.getCoordinator() != null) {
            SellerEditRequest.CoordinatorDTO coord = request.getCoordinator();
            pendingSeller.setCoordinatorName(coord.getName());
            pendingSeller.setCoordinatorDesignation(coord.getDesignation());
            pendingSeller.setCoordinatorEmail(coord.getEmail());
            pendingSeller.setCoordinatorMobile(coord.getMobile());
        }

        // Bank details fields
        if (request.getBankDetails() != null) {
            SellerEditRequest.BankDetailsDTO bank = request.getBankDetails();
            pendingSeller.setBankName(bank.getBankName());
            pendingSeller.setBankBranch(bank.getBranch());
            pendingSeller.setBankIfscCode(bank.getIfscCode());
            pendingSeller.setBankAccountNumber(bank.getAccountNumber());
            pendingSeller.setBankAccountHolderName(bank.getAccountHolderName());
            pendingSeller.setBankDocumentFileUrl(bank.getBankDocumentFileUrl());
        }

        // GST fields
        pendingSeller.setGstNumber(request.getGstNumber());
        pendingSeller.setGstFileUrl(request.getGstFileUrl());

        // Company Registration Certificate
        pendingSeller.setCompanyRegistrationCertificateUrl(request.getCompanyRegistrationCertificateUrl());

        // Product Types
        if (request.getProductTypeId() != null && !request.getProductTypeId().isEmpty()) {
            List<ProductTypeMaster> productTypes = productTypeRepository.findAllById(request.getProductTypeId());
            if (productTypes.size() != request.getProductTypeId().size()) {
                throw new ResourceNotFoundException("One or more product types not found");
            }
            pendingSeller.setProductTypes(productTypes);
        }

        pendingSeller.setRequestedBy(requestedBy);
        pendingSeller.setCreatedAt(LocalDateTime.now());

        return pendingSeller;
    }

    @Transactional
    public void approveSellerUpdate(Long pendingSellerId, String approvedBy) {
        PendingSeller pendingSeller = pendingSellerRepository.findById(pendingSellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Pending request not found with id: " + pendingSellerId));

        if (!"PENDING".equals(pendingSeller.getStatus())) {
            throw new IllegalStateException("Request is already " + pendingSeller.getStatus());
        }

        try {
            String now = LocalDateTime.now().format(TS_FORMATTER);
            String sellerId = null;

            if ("UPDATE".equals(pendingSeller.getRequestType())) {
                Seller seller = sellerRepository.findById(pendingSeller.getSellerId())
                        .orElseThrow(() -> new ResourceNotFoundException("Seller not found with id: " + pendingSeller.getSellerId()));

                sellerId = seller.getSellerId();

                // ── Step 1: Snapshot the CURRENT live state before anything changes ──
                sellerHistoryService.snapshotBeforeUpdate(seller, approvedBy);

                // ── Step 2: Capture old coordinator email before it is overwritten ──
                String oldCoordinatorEmail = (seller.getCoordinator() != null)
                        ? seller.getCoordinator().getEmail()
                        : null;

                // ── Step 3: Move files from pendingsellers/ → sellers/ and update URLs before applying to seller
                movePendingFilesToSeller(pendingSeller, sellerId, now);

                // ── Step 4: Apply pending data to live Seller and persist ──────────
                updateSellerFromPending(seller, pendingSeller);
                sellerRepository.save(seller);
                log.info("Updated existing seller with ID: {}", sellerId);

                // ── Step 5: Rotate credentials if coordinator email changed ────────
                sellerHistoryService.rotateCoordinatorCredentialsIfEmailChanged(
                        seller, oldCoordinatorEmail);

            } else if ("CREATE".equals(pendingSeller.getRequestType())) {
                sellerId = generateSellerId();

                // Move files from pendingsellers/ → sellers/ and update URLs before creating seller
                movePendingFilesToSeller(pendingSeller, sellerId, now);

                Seller newSeller = createSellerFromPending(pendingSeller);
                newSeller.setSellerId(sellerId);
                sellerRepository.save(newSeller);
                log.info("Created new seller with ID: {}", sellerId);
            }

            sendApprovalEmail(pendingSeller, sellerId, approvedBy);

            // Update pending record status instead of deleting it
            pendingSeller.setStatus("APPROVED");
            pendingSeller.setApprovedBy(approvedBy);
            pendingSeller.setApprovedAt(LocalDateTime.now());
            pendingSellerRepository.save(pendingSeller);

            log.info("Seller update approved by: {} for pending request ID: {}", approvedBy, pendingSellerId);

        } catch (Exception e) {
            log.error("Failed to approve seller update for pending request ID: {}", pendingSellerId, e);
            pendingSeller.setStatus("APPROVAL_FAILED");
            pendingSeller.setRejectionReason("Failed to update main table: " + e.getMessage());
            pendingSellerRepository.save(pendingSeller);
            throw new RuntimeException("Failed to approve seller update: " + e.getMessage(), e);
        }
    }

    private void updateSellerFromPending(Seller seller, PendingSeller pending) {
        seller.setSellerName(pending.getSellerName());
        seller.setPhone(pending.getPhone());
        seller.setEmail(pending.getEmail());
        seller.setWebsite(pending.getWebsite());
        seller.setTermsAccepted(pending.isTermsAccepted());
        seller.setCompanyType(pending.getCompanyType());
        seller.setSellerType(pending.getSellerType());
        seller.setCompanyRegistrationCertificateUrl(pending.getCompanyRegistrationCertificateUrl());

        // Update or create Address - CHECK FOR EXISTING FIRST
        if (pending.getState() != null) {
            SellerAddress address = seller.getAddress();
            if (address == null) {
                address = new SellerAddress();
                address.setSeller(seller);
                address.setCreatedBy("system");
                address.setCreatedAt(LocalDateTime.now());
            }
            address.setState(pending.getState());
            address.setDistrict(pending.getDistrict());
            address.setTaluka(pending.getTaluka());
            address.setCity(pending.getCity());
            address.setStreet(pending.getStreet());
            address.setBuildingNo(pending.getBuildingNo());
            address.setLandmark(pending.getLandmark());
            address.setPinCode(pending.getPinCode());
            address.setUpdatedBy("system");
            address.setUpdatedAt(LocalDateTime.now());
            seller.setAddress(address);
        }

        // Update or create Coordinator - CHECK FOR EXISTING FIRST
        if (pending.getCoordinatorName() != null) {
            SellerCoordinator coordinator = seller.getCoordinator();
            if (coordinator == null) {
                coordinator = new SellerCoordinator();
                coordinator.setSeller(seller);
                coordinator.setCreatedBy("system");
                coordinator.setCreatedAt(LocalDateTime.now());
            }
            coordinator.setName(pending.getCoordinatorName());
            coordinator.setDesignation(pending.getCoordinatorDesignation());
            coordinator.setEmail(pending.getCoordinatorEmail());
            coordinator.setMobile(pending.getCoordinatorMobile());
            coordinator.setUpdatedBy("system");
            coordinator.setUpdatedAt(LocalDateTime.now());
            seller.setCoordinator(coordinator);
        }

        // Update or create Bank Details - CHECK FOR EXISTING FIRST
        if (pending.getBankName() != null) {
            SellerBankDetails bankDetails = seller.getBankDetails();
            if (bankDetails == null) {
                bankDetails = new SellerBankDetails();
                bankDetails.setSeller(seller);
                bankDetails.setCreatedBy("system");
                bankDetails.setCreatedAt(LocalDateTime.now());
            }
            bankDetails.setBankName(pending.getBankName());
            bankDetails.setBranch(pending.getBankBranch());
            bankDetails.setIfscCode(pending.getBankIfscCode());
            bankDetails.setAccountNumber(pending.getBankAccountNumber());
            bankDetails.setAccountHolderName(pending.getBankAccountHolderName());
            bankDetails.setBankDocumentFileUrl(pending.getBankDocumentFileUrl());
            bankDetails.setUpdatedBy("system");
            bankDetails.setUpdatedAt(LocalDateTime.now());
            seller.setBankDetails(bankDetails);
        }

        // Update or create GST - CHECK FOR EXISTING FIRST
        if (pending.getGstNumber() != null) {
            SellerGST gst = seller.getSellerGST();
            if (gst == null) {
                gst = new SellerGST();
                gst.setSeller(seller);
            }
            gst.setGstNumber(pending.getGstNumber());
            gst.setGstFileUrl(pending.getGstFileUrl());
            seller.setSellerGST(gst);
        }

        // Handle Documents - Clear and add new ones (documents can be replaced)
        if (pending.getDocuments() != null && !pending.getDocuments().isEmpty()) {
            // Clear existing documents
            seller.getDocuments().clear();

            // Add new documents
            for (PendingSellerDocument pendingDoc : pending.getDocuments()) {
                SellerDocument document = new SellerDocument();
                document.setSeller(seller);
                document.setProductTypes(pendingDoc.getProductType());
                document.setDocumentNumber(pendingDoc.getDocumentNumber());
                document.setDocumentFileUrl(pendingDoc.getDocumentFileUrl());
                document.setLicenseIssueDate(pendingDoc.getLicenseIssueDate());
                document.setLicenseExpiryDate(pendingDoc.getLicenseExpiryDate());
                document.setLicenseIssuingAuthority(pendingDoc.getLicenseIssuingAuthority());
                document.setLicenseStatus(pendingDoc.getLicenseStatus());
                document.setCreatedBy("system");
                document.setCreatedAt(LocalDateTime.now());
                seller.getDocuments().add(document);
            }
        }

        // Update Product Types
        if (pending.getProductTypes() != null) {
            seller.getProductTypes().clear();
            seller.getProductTypes().addAll(pending.getProductTypes());
        }

        seller.setUpdatedBy("system");
        seller.setUpdatedAt(LocalDateTime.now());
    }

    private Seller createSellerFromPending(PendingSeller pending) {
        Seller seller = new Seller();

        // Set basic fields
        seller.setSellerName(pending.getSellerName());
        seller.setPhone(pending.getPhone());
        seller.setEmail(pending.getEmail());
        seller.setWebsite(pending.getWebsite());
        seller.setTermsAccepted(pending.isTermsAccepted());
        seller.setCompanyType(pending.getCompanyType());
        seller.setSellerType(pending.getSellerType());
        seller.setCompanyRegistrationCertificateUrl(pending.getCompanyRegistrationCertificateUrl());
        seller.setCreatedBy("system");
        seller.setCreatedAt(LocalDateTime.now());
        seller.setStatus("ACTIVE");
        seller.setPhoneVerified(false);
        seller.setEmailVerified(false);
        seller.setIsActive(true);

        // Create Address
        if (pending.getState() != null) {
            SellerAddress address = new SellerAddress();
            address.setSeller(seller);
            address.setState(pending.getState());
            address.setDistrict(pending.getDistrict());
            address.setTaluka(pending.getTaluka());
            address.setCity(pending.getCity());
            address.setStreet(pending.getStreet());
            address.setBuildingNo(pending.getBuildingNo());
            address.setLandmark(pending.getLandmark());
            address.setPinCode(pending.getPinCode());
            address.setCreatedBy("system");
            address.setCreatedAt(LocalDateTime.now());
            seller.setAddress(address);
        }

        // Create Coordinator
        if (pending.getCoordinatorName() != null) {
            SellerCoordinator coordinator = new SellerCoordinator();
            coordinator.setSeller(seller);
            coordinator.setName(pending.getCoordinatorName());
            coordinator.setDesignation(pending.getCoordinatorDesignation());
            coordinator.setEmail(pending.getCoordinatorEmail());
            coordinator.setMobile(pending.getCoordinatorMobile());
            coordinator.setCreatedBy("system");
            coordinator.setCreatedAt(LocalDateTime.now());
            seller.setCoordinator(coordinator);
        }

        // Create Bank Details
        if (pending.getBankName() != null) {
            SellerBankDetails bankDetails = new SellerBankDetails();
            bankDetails.setSeller(seller);
            bankDetails.setBankName(pending.getBankName());
            bankDetails.setBranch(pending.getBankBranch());
            bankDetails.setIfscCode(pending.getBankIfscCode());
            bankDetails.setAccountNumber(pending.getBankAccountNumber());
            bankDetails.setAccountHolderName(pending.getBankAccountHolderName());
            bankDetails.setBankDocumentFileUrl(pending.getBankDocumentFileUrl());
            bankDetails.setCreatedBy("system");
            bankDetails.setCreatedAt(LocalDateTime.now());
            seller.setBankDetails(bankDetails);
        }

        // Create GST
        if (pending.getGstNumber() != null) {
            SellerGST gst = new SellerGST();
            gst.setSeller(seller);
            gst.setGstNumber(pending.getGstNumber());
            gst.setGstFileUrl(pending.getGstFileUrl());
            seller.setSellerGST(gst);
        }

        // Create Documents
        if (pending.getDocuments() != null && !pending.getDocuments().isEmpty()) {
            for (PendingSellerDocument pendingDoc : pending.getDocuments()) {
                SellerDocument document = new SellerDocument();
                document.setSeller(seller);
                document.setProductTypes(pendingDoc.getProductType());
                document.setDocumentNumber(pendingDoc.getDocumentNumber());
                document.setDocumentFileUrl(pendingDoc.getDocumentFileUrl());
                document.setLicenseIssueDate(pendingDoc.getLicenseIssueDate());
                document.setLicenseExpiryDate(pendingDoc.getLicenseExpiryDate());
                document.setLicenseIssuingAuthority(pendingDoc.getLicenseIssuingAuthority());
                document.setLicenseStatus(pendingDoc.getLicenseStatus());
                document.setCreatedBy("system");
                document.setCreatedAt(LocalDateTime.now());
                seller.getDocuments().add(document);
            }
        }

        // Set Product Types
        if (pending.getProductTypes() != null) {
            seller.getProductTypes().clear();
            seller.getProductTypes().addAll(pending.getProductTypes());
        }

        return seller;
    }

    private String generateSellerId() {
        return "SELL" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    @Transactional
    public void rejectSellerUpdate(Long pendingSellerId, String rejectionReason, String approvedBy) {
        PendingSeller pendingSeller = pendingSellerRepository.findById(pendingSellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Pending request not found with id: " + pendingSellerId));

        if (!"PENDING".equals(pendingSeller.getStatus())) {
            throw new IllegalStateException("Request is already " + pendingSeller.getStatus());
        }

        // Send rejection email (HTML format) before updating status
        sendRejectionEmail(pendingSeller, rejectionReason, approvedBy);

        // Keep for audit
        pendingSeller.setStatus("REJECTED");
        pendingSeller.setRejectionReason(rejectionReason);
        pendingSeller.setApprovedBy(approvedBy);
        pendingSeller.setApprovedAt(LocalDateTime.now());
        pendingSellerRepository.save(pendingSeller);

        log.info("Seller update rejected by: {} for pending request ID: {}. Reason: {}", approvedBy, pendingSellerId, rejectionReason);
    }

    @Transactional
    public void approveAndDeleteAll(List<Long> pendingSellerIds, String approvedBy) {
        for (Long id : pendingSellerIds) {
            approveSellerUpdate(id, approvedBy);
        }
    }

    private void sendApprovalEmail(PendingSeller pendingSeller, String sellerId, String approvedBy) {
        String coordinatorEmail = pendingSeller.getCoordinatorEmail();
        String coordinatorName = pendingSeller.getCoordinatorName();
        String sellerName = pendingSeller.getSellerName();
        String requestType = pendingSeller.getRequestType();

        if (coordinatorEmail == null || coordinatorEmail.isEmpty()) {
            log.warn("No coordinator email found for pending seller ID: {}", pendingSeller.getPendingSellerId());
            return;
        }

        coordinatorEmailService.sendSellerProfileApproved(
                coordinatorEmail,
                coordinatorName,
                sellerName,
                sellerId,
                approvedBy,
                requestType
        );
    }

    private void sendRejectionEmail(PendingSeller pendingSeller, String rejectionReason, String rejectedBy) {
        String coordinatorEmail = pendingSeller.getCoordinatorEmail();
        String coordinatorName = pendingSeller.getCoordinatorName();
        String sellerName = pendingSeller.getSellerName();
        String requestType = pendingSeller.getRequestType();

        if (coordinatorEmail == null || coordinatorEmail.isEmpty()) {
            log.warn("No coordinator email found for pending seller ID: {}", pendingSeller.getPendingSellerId());
            return;
        }

        coordinatorEmailService.sendSellerProfileRejected(
                coordinatorEmail,
                coordinatorName,
                sellerName,
                rejectionReason,
                rejectedBy,
                requestType
        );
    }

    public List<PendingSellerResponseDTO> getPendingRequests() {
        // Return only requests that are still PENDING (not auto-approved or processed)
        List<PendingSeller> pendingSellers = pendingSellerRepository.findByStatus("PENDING");

        return pendingSellers.stream()
                .map(this::mapToPendingResponseDTO)
                .collect(Collectors.toList());
    }

    // New method to get all requests including auto-approved ones for audit
    public List<PendingSellerResponseDTO> getAllRequests() {
        List<PendingSeller> allSellers = pendingSellerRepository.findAll();
        return allSellers.stream()
                .map(this::mapToPendingResponseDTO)
                .collect(Collectors.toList());
    }

    // New method to get auto-approved requests
    public List<PendingSellerResponseDTO> getAutoApprovedRequests() {
        List<PendingSeller> autoApprovedSellers = pendingSellerRepository.findByStatus("AUTO_APPROVED");
        return autoApprovedSellers.stream()
                .map(this::mapToPendingResponseDTO)
                .collect(Collectors.toList());
    }

    private PendingSellerResponseDTO mapToPendingResponseDTO(PendingSeller pendingSeller) {
        PendingSellerResponseDTO dto = new PendingSellerResponseDTO();

        // Basic info
        dto.setPendingSellerId(pendingSeller.getPendingSellerId());
        dto.setSellerId(pendingSeller.getSellerId());
        dto.setSellerName(pendingSeller.getSellerName());
        dto.setRequestType(pendingSeller.getRequestType());
        dto.setStatus(pendingSeller.getStatus());
        dto.setRequestedBy(pendingSeller.getRequestedBy());
        dto.setCreatedAt(pendingSeller.getCreatedAt());

        // Contact info
        dto.setPhone(pendingSeller.getPhone());
        dto.setEmail(pendingSeller.getEmail());
        dto.setWebsite(pendingSeller.getWebsite());
        dto.setTermsAccepted(pendingSeller.isTermsAccepted());

        // Company and Seller Types
        if (pendingSeller.getCompanyType() != null) {
            dto.setCompanyTypeId(pendingSeller.getCompanyType().getCompanyTypeId());
            dto.setCompanyTypeName(pendingSeller.getCompanyType().getCompanyTypeName());
        }

        if (pendingSeller.getSellerType() != null) {
            dto.setSellerTypeId(pendingSeller.getSellerType().getSellerTypeId());
            dto.setSellerTypeName(pendingSeller.getSellerType().getSellerTypeName());
        }

        // Address
        if (pendingSeller.getState() != null) {
            PendingSellerResponseDTO.AddressDTO addressDTO = new PendingSellerResponseDTO.AddressDTO();
            addressDTO.setStateId(pendingSeller.getState().getStateId());
            addressDTO.setStateName(pendingSeller.getState().getStateName());
            addressDTO.setDistrictId(pendingSeller.getDistrict() != null ? pendingSeller.getDistrict().getDistrictId() : null);
            addressDTO.setDistrictName(pendingSeller.getDistrict() != null ? pendingSeller.getDistrict().getDistrictName() : null);
            addressDTO.setTalukaId(pendingSeller.getTaluka() != null ? pendingSeller.getTaluka().getTalukaId() : null);
            addressDTO.setTalukaName(pendingSeller.getTaluka() != null ? pendingSeller.getTaluka().getTalukaName() : null);
            addressDTO.setCity(pendingSeller.getCity());
            addressDTO.setStreet(pendingSeller.getStreet());
            addressDTO.setBuildingNo(pendingSeller.getBuildingNo());
            addressDTO.setLandmark(pendingSeller.getLandmark());
            addressDTO.setPinCode(pendingSeller.getPinCode());
            dto.setAddress(addressDTO);
        }

        // Coordinator
        if (pendingSeller.getCoordinatorName() != null) {
            PendingSellerResponseDTO.CoordinatorDTO coordinatorDTO = new PendingSellerResponseDTO.CoordinatorDTO();
            coordinatorDTO.setName(pendingSeller.getCoordinatorName());
            coordinatorDTO.setDesignation(pendingSeller.getCoordinatorDesignation());
            coordinatorDTO.setEmail(pendingSeller.getCoordinatorEmail());
            coordinatorDTO.setMobile(pendingSeller.getCoordinatorMobile());
            dto.setCoordinator(coordinatorDTO);
        }

        // Bank Details
        if (pendingSeller.getBankName() != null) {
            PendingSellerResponseDTO.BankDetailsDTO bankDTO = new PendingSellerResponseDTO.BankDetailsDTO();
            bankDTO.setBankName(pendingSeller.getBankName());
            bankDTO.setBranch(pendingSeller.getBankBranch());
            bankDTO.setIfscCode(pendingSeller.getBankIfscCode());
            bankDTO.setAccountNumber(pendingSeller.getBankAccountNumber());
            bankDTO.setAccountHolderName(pendingSeller.getBankAccountHolderName());
            bankDTO.setBankDocumentFileUrl(pendingSeller.getBankDocumentFileUrl());
            dto.setBankDetails(bankDTO);
        }

        // GST
        dto.setGstNumber(pendingSeller.getGstNumber());
        dto.setGstFileUrl(pendingSeller.getGstFileUrl());

        // Company Registration Certificate
        dto.setCompanyRegistrationCertificateUrl(pendingSeller.getCompanyRegistrationCertificateUrl());

        // Product Types
        if (pendingSeller.getProductTypes() != null && !pendingSeller.getProductTypes().isEmpty()) {
            List<PendingSellerResponseDTO.ProductTypeDTO> productTypeDTOs = pendingSeller.getProductTypes().stream()
                    .map(pt -> {
                        PendingSellerResponseDTO.ProductTypeDTO ptDTO = new PendingSellerResponseDTO.ProductTypeDTO();
                        ptDTO.setProductTypeId(pt.getProductTypeId());
                        ptDTO.setProductTypeName(pt.getProductTypeName());
                        return ptDTO;
                    })
                    .collect(Collectors.toList());
            dto.setProductTypes(productTypeDTOs);
        }

        // Documents
        if (pendingSeller.getDocuments() != null && !pendingSeller.getDocuments().isEmpty()) {
            List<PendingSellerResponseDTO.DocumentDTO> documentDTOs = pendingSeller.getDocuments().stream()
                    .map(this::mapDocumentToDTO)
                    .collect(Collectors.toList());
            dto.setDocuments(documentDTOs);
        }

        return dto;
    }

    private PendingSellerResponseDTO.DocumentDTO mapDocumentToDTO(PendingSellerDocument document) {
        PendingSellerResponseDTO.DocumentDTO dto = new PendingSellerResponseDTO.DocumentDTO();
        dto.setProductTypeId(document.getProductType().getProductTypeId());
        dto.setProductTypeName(document.getProductType().getProductTypeName());
        dto.setDocumentNumber(document.getDocumentNumber());
        dto.setDocumentFileUrl(document.getDocumentFileUrl());
        dto.setLicenseIssueDate(document.getLicenseIssueDate() != null ?
                document.getLicenseIssueDate().atStartOfDay() : null);
        dto.setLicenseExpiryDate(document.getLicenseExpiryDate() != null ?
                document.getLicenseExpiryDate().atStartOfDay() : null);
        dto.setLicenseIssuingAuthority(document.getLicenseIssuingAuthority());
        return dto;
    }

    /**
     * Maps a PendingSeller entity to SellerResponseDTO
     * FIXED: Now properly sets pendingSellerId in all cases
     */
    private SellerResponseDTO mapToResponseDTO(PendingSeller savedPending) {
        SellerResponseDTO response = new SellerResponseDTO();

        // ALWAYS set the pendingSellerId first - this is the critical fix
        response.setPendingSellerId(savedPending.getPendingSellerId());

        // Set message based on status
        if ("AUTO_APPROVED".equals(savedPending.getStatus())) {
            response.setMessage("Your changes have been auto-approved and applied successfully");
        } else {
            response.setMessage("Update request submitted successfully and pending admin approval");
        }

        response.setSellerName(savedPending.getSellerName());
        response.setPhone(savedPending.getPhone());
        response.setEmail(savedPending.getEmail());
        response.setWebsite(savedPending.getWebsite());
        response.setGstNumber(savedPending.getGstNumber());
        response.setGstFileUrl(savedPending.getGstFileUrl());
        response.setTermsAccepted(savedPending.isTermsAccepted());
        response.setCompanyRegistrationCertificateUrl(savedPending.getCompanyRegistrationCertificateUrl());

        if (savedPending.getCompanyType() != null) {
            response.setCompanyTypeId(savedPending.getCompanyType().getCompanyTypeId());
        }
        if (savedPending.getSellerType() != null) {
            response.setSellerTypeId(savedPending.getSellerType().getSellerTypeId());
        }
        if (savedPending.getProductTypes() != null && !savedPending.getProductTypes().isEmpty()) {
            response.setProductTypeId(
                    savedPending.getProductTypes().stream()
                            .map(ProductTypeMaster::getProductTypeId)
                            .collect(Collectors.toList())
            );
        }

        if (savedPending.getState() != null) {
            SellerResponseDTO.AddressDTO addr = new SellerResponseDTO.AddressDTO();
            addr.setStateId(savedPending.getState().getStateId());
            addr.setDistrictId(savedPending.getDistrict() != null ? savedPending.getDistrict().getDistrictId() : null);
            addr.setTalukaId(savedPending.getTaluka() != null ? savedPending.getTaluka().getTalukaId() : null);
            addr.setCity(savedPending.getCity());
            addr.setStreet(savedPending.getStreet());
            addr.setBuildingNo(savedPending.getBuildingNo());
            addr.setLandmark(savedPending.getLandmark());
            addr.setPinCode(savedPending.getPinCode());
            response.setAddress(addr);
        }

        if (savedPending.getCoordinatorName() != null) {
            SellerResponseDTO.CoordinatorDTO coord = new SellerResponseDTO.CoordinatorDTO();
            coord.setName(savedPending.getCoordinatorName());
            coord.setDesignation(savedPending.getCoordinatorDesignation());
            coord.setEmail(savedPending.getCoordinatorEmail());
            coord.setMobile(savedPending.getCoordinatorMobile());
            response.setCoordinator(coord);
        }

        if (savedPending.getBankName() != null) {
            SellerResponseDTO.BankDetailsDTO bank = new SellerResponseDTO.BankDetailsDTO();
            bank.setBankName(savedPending.getBankName());
            bank.setBranch(savedPending.getBankBranch());
            bank.setIfscCode(savedPending.getBankIfscCode());
            bank.setAccountNumber(savedPending.getBankAccountNumber());
            bank.setAccountHolderName(savedPending.getBankAccountHolderName());
            bank.setBankDocumentFileUrl(savedPending.getBankDocumentFileUrl());
            response.setBankDetails(bank);
        }

        if (savedPending.getDocuments() != null && !savedPending.getDocuments().isEmpty()) {
            List<SellerResponseDTO.DocumentDTO> documentDTOs = savedPending.getDocuments().stream()
                    .map(doc -> {
                        SellerResponseDTO.DocumentDTO dto = new SellerResponseDTO.DocumentDTO();
                        dto.setPendingSellerDocumentId(doc.getId());
                        dto.setProductTypeId(doc.getProductType().getProductTypeId());
                        dto.setProductTypeName(doc.getProductType().getProductTypeName());
                        dto.setDocumentNumber(doc.getDocumentNumber());
                        dto.setDocumentFileUrl(doc.getDocumentFileUrl());
                        dto.setLicenseIssueDate(doc.getLicenseIssueDate());
                        dto.setLicenseExpiryDate(doc.getLicenseExpiryDate());
                        dto.setLicenseIssuingAuthority(doc.getLicenseIssuingAuthority());
                        return dto;
                    })
                    .collect(Collectors.toList());
            response.setDocuments(documentDTOs);
        }

        return response;
    }

    @Transactional
    public void deleteOldRejectedRequests(LocalDateTime cutoffDate) {
        List<PendingSeller> rejectedRequests = pendingSellerRepository.findByStatusAndCreatedAtBefore("REJECTED", cutoffDate);
        for (PendingSeller request : rejectedRequests) {
            if (request.getDocuments() != null && !request.getDocuments().isEmpty()) {
                pendingSellerDocumentRepository.deleteAll(request.getDocuments());
            }
        }
        pendingSellerRepository.deleteAll(rejectedRequests);
        log.info("Deleted {} old rejected requests created before {}", rejectedRequests.size(), cutoffDate);
    }

    // New method to delete old auto-approved requests (optional)
    @Transactional
    public void deleteOldAutoApprovedRequests(LocalDateTime cutoffDate) {
        List<PendingSeller> autoApprovedRequests = pendingSellerRepository.findByStatusAndCreatedAtBefore("AUTO_APPROVED", cutoffDate);
        for (PendingSeller request : autoApprovedRequests) {
            if (request.getDocuments() != null && !request.getDocuments().isEmpty()) {
                pendingSellerDocumentRepository.deleteAll(request.getDocuments());
            }
        }
        pendingSellerRepository.deleteAll(autoApprovedRequests);
        log.info("Deleted {} old auto-approved requests created before {}", autoApprovedRequests.size(), cutoffDate);
    }

    private void movePendingFilesToSeller(PendingSeller pending, String sellerId, String now) {
        // ── GST file ──────────────────────────────────────────────────────────
        if (isRealUrl(pending.getGstFileUrl()) && isPendingUrl(pending.getGstFileUrl())) {
            String ext       = extractExtension(pending.getGstFileUrl());
            String targetKey = String.format("sellers/%s/gst/GST_IMAGE_%s.%s", sellerId, now, ext);
            String newUrl    = s3Service.copyFile(s3Service.extractKeyFromUrl(pending.getGstFileUrl()), targetKey);
            s3Service.deleteFile(s3Service.extractKeyFromUrl(pending.getGstFileUrl()));
            pending.setGstFileUrl(newUrl);
            log.info("GST file moved → {}", newUrl);
        }

        // ── Bank document ─────────────────────────────────────────────────────
        if (isRealUrl(pending.getBankDocumentFileUrl()) && isPendingUrl(pending.getBankDocumentFileUrl())) {
            String ext       = extractExtension(pending.getBankDocumentFileUrl());
            String targetKey = String.format("sellers/%s/bankdocument/BANK_DETAILS_%s.%s", sellerId, now, ext);
            String newUrl    = s3Service.copyFile(s3Service.extractKeyFromUrl(pending.getBankDocumentFileUrl()), targetKey);
            s3Service.deleteFile(s3Service.extractKeyFromUrl(pending.getBankDocumentFileUrl()));
            pending.setBankDocumentFileUrl(newUrl);
            log.info("Bank document moved → {}", newUrl);
        }

        // ── Company Registration Certificate ──────────────────────────────────
        if (isRealUrl(pending.getCompanyRegistrationCertificateUrl()) && isPendingUrl(pending.getCompanyRegistrationCertificateUrl())) {
            String ext       = extractExtension(pending.getCompanyRegistrationCertificateUrl());
            String targetKey = String.format("sellers/%s/companyregistrationcertificate/COMPANY_REGISTRATION_CERTIFICATE_%s.%s", sellerId, now, ext);
            String newUrl    = s3Service.copyFile(s3Service.extractKeyFromUrl(pending.getCompanyRegistrationCertificateUrl()), targetKey);
            s3Service.deleteFile(s3Service.extractKeyFromUrl(pending.getCompanyRegistrationCertificateUrl()));
            pending.setCompanyRegistrationCertificateUrl(newUrl);
            log.info("Company Registration Certificate moved → {}", newUrl);
        }

        // ── License / document files ──────────────────────────────────────────
        if (pending.getDocuments() != null) {
            for (PendingSellerDocument doc : pending.getDocuments()) {
                if (isRealUrl(doc.getDocumentFileUrl()) && isPendingUrl(doc.getDocumentFileUrl())) {
                    String ext       = extractExtension(doc.getDocumentFileUrl());
                    String safeName  = doc.getProductType().getProductTypeName()
                            .trim()
                            .replaceAll("[\\s/\\\\:*?\"<>|#]+", "_")
                            .replaceAll("_+", "_");
                    String targetKey = String.format("sellers/%s/licenses/%s_%s.%s", sellerId, safeName, now, ext);
                    String newUrl    = s3Service.copyFile(s3Service.extractKeyFromUrl(doc.getDocumentFileUrl()), targetKey);
                    s3Service.deleteFile(s3Service.extractKeyFromUrl(doc.getDocumentFileUrl()));
                    doc.setDocumentFileUrl(newUrl);
                    log.info("License '{}' moved → {}", safeName, newUrl);
                }
            }
        }
    }

    private boolean isPendingUrl(String url) {
        return url != null && url.contains("/pendingsellers/");
    }

    private boolean isRealUrl(String url) {
        return url != null && !url.isBlank() && !"PENDING".equalsIgnoreCase(url.trim());
    }

    private String extractExtension(String url) {
        int dot = url.lastIndexOf('.');
        int slash = url.lastIndexOf('/');
        return (dot > slash && dot < url.length() - 1) ? url.substring(dot + 1).toLowerCase() : "bin";
    }

    @Transactional
    public void deletePendingSeller(Long pendingSellerId) {
        PendingSeller pendingSeller = pendingSellerRepository.findById(pendingSellerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pending seller not found with id: " + pendingSellerId));

        pendingSellerRepository.delete(pendingSeller);

        log.info("Pending seller record deleted (rollback) for pendingSellerId={}", pendingSellerId);
    }

    public PendingSellerResponseDTO getPendingSellerById(Long pendingSellerId) {
        PendingSeller pendingSeller = pendingSellerRepository.findById(pendingSellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Pending seller not found with id: " + pendingSellerId));

        return mapToPendingResponseDTO(pendingSeller);
    }

    @Transactional(readOnly = true)
    public SellerResponseDTO findSellerById(String sellerId) {
        Seller seller = sellerRepository.findBySellerId(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found with id: " + sellerId));

        return sellerByIdMapper.toResponseDTO(seller);
    }

    /**
     * Send acknowledgement email when admin approval is required
     */
    private void sendUpdateSubmissionAcknowledgementEmail(PendingSeller pendingSeller, String sellerName) {
        String coordinatorEmail = pendingSeller.getCoordinatorEmail();
        String coordinatorName = pendingSeller.getCoordinatorName();
        String requestId = String.valueOf(pendingSeller.getPendingSellerId());

        if (coordinatorEmail == null || coordinatorEmail.isEmpty()) {
            log.warn("No coordinator email found for pending seller ID: {}", pendingSeller.getPendingSellerId());
            return;
        }
        Seller existingSeller = sellerRepository.findById(pendingSeller.getSellerId())
                .orElse(null);

        String oldSellerName = existingSeller != null ? existingSeller.getSellerName() : "Unknown";

        SellerUpdateEmailDTO emailDTO = SellerUpdateEmailDTO.builder()
                .coordinatorEmail(coordinatorEmail)
                .coordinatorName(coordinatorName != null ? coordinatorName : "Coordinator")
                .sellerCompanyName(oldSellerName)
                .requestId(requestId)
                .supportEmail(supportEmail)
                .build();

        coordinatorEmailService.sendSellerUpdateSubmissionAcknowledgement(emailDTO);
        log.info("Update submission acknowledgement email sent to coordinator: {} for pending request ID: {}",
                coordinatorEmail, pendingSeller.getPendingSellerId());
    }
}