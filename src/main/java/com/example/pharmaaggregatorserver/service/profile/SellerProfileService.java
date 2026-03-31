package com.example.pharmaaggregatorserver.service.profile;

import com.example.pharmaaggregatorserver.dto.seller.profile.PendingSellerResponseDTO;
import com.example.pharmaaggregatorserver.dto.seller.profile.SellerEditRequest;
import com.example.pharmaaggregatorserver.dto.seller.profile.SellerResponseDTO;
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
import com.example.pharmaaggregatorserver.mapper.seller.profile.SellerByIdMapper;
import com.example.pharmaaggregatorserver.repository.master.*;
import com.example.pharmaaggregatorserver.repository.seller.SellerRepository;
import com.example.pharmaaggregatorserver.repository.seller.profile.PendingSellerDocumentRepository;
import com.example.pharmaaggregatorserver.repository.seller.profile.PendingSellerRepository;
import com.example.pharmaaggregatorserver.service.EmailService;
import com.example.pharmaaggregatorserver.service.S3Service;
import com.example.pharmaaggregatorserver.service.seller.history.SellerHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final EmailService emailService;  // Inject EmailService
    private final S3Service s3Service;
    private final SellerHistoryService sellerHistoryService;

    private static final DateTimeFormatter TS_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Transactional
    public SellerResponseDTO requestSellerUpdate(String sellerId, SellerEditRequest request, String requestedBy) {

        // Check if there's already a pending request for this seller
        List<PendingSeller> pendingRequests = pendingSellerRepository.findBySellerIdAndStatus(sellerId, "PENDING");
        if (!pendingRequests.isEmpty()) {
            throw new IllegalStateException("A pending update request already exists for this seller");
        }

        // Fetch existing seller to validate it exists
        sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found with id: " + sellerId));

        // Create pending seller entity
        PendingSeller pendingSeller = createPendingSellerFromRequest(request, requestedBy);
        pendingSeller.setSellerId(sellerId);
        pendingSeller.setRequestType("UPDATE");

        PendingSeller savedPending = pendingSellerRepository.save(pendingSeller);

        // Save documents separately
        if (request.getDocuments() != null && !request.getDocuments().isEmpty()) {
            saveDocuments(savedPending, request.getDocuments());
        }

        // Refresh to load documents
        savedPending = pendingSellerRepository.findById(savedPending.getPendingSellerId())
                .orElseThrow(() -> new ResourceNotFoundException("Pending seller not found"));

        // Return response
        return mapToResponseDTO(savedPending);
    }

    @Transactional
    public PendingSeller createPendingSeller(SellerEditRequest request, String requestedBy) {
        PendingSeller pendingSeller = createPendingSellerFromRequest(request, requestedBy);
        pendingSeller.setRequestType("CREATE");
        PendingSeller savedPending = pendingSellerRepository.save(pendingSeller);

        // Save documents separately
        if (request.getDocuments() != null && !request.getDocuments().isEmpty()) {
            saveDocuments(savedPending, request.getDocuments());
        }

        return savedPending;
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
        pendingSeller.setStatus("PENDING");

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
                // Called AFTER save so the new coordinator email is already in DB.
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

            // Delete pending documents and pending seller record
            if (pendingSeller.getDocuments() != null && !pendingSeller.getDocuments().isEmpty()) {
                pendingSellerDocumentRepository.deleteAll(pendingSeller.getDocuments());
            }
            pendingSellerRepository.delete(pendingSeller);

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
        // If coordinator email is different/changes update the username and send mail to new coordinator
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

    /**
     * Send approval email to coordinator using HTML format
     */
    private void sendApprovalEmail(PendingSeller pendingSeller, String sellerId, String approvedBy) {
        String coordinatorEmail = pendingSeller.getCoordinatorEmail();
        String coordinatorName = pendingSeller.getCoordinatorName();
        String sellerName = pendingSeller.getSellerName();
        String requestType = pendingSeller.getRequestType();

        if (coordinatorEmail == null || coordinatorEmail.isEmpty()) {
            log.warn("No coordinator email found for pending seller ID: {}", pendingSeller.getPendingSellerId());
            return;
        }

        String action = "CREATE".equals(requestType) ? "Created" : "Updated";
        String subject = String.format("Seller Profile %s Approved - %s", action, sellerName);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        String approvalTime = LocalDateTime.now().format(formatter);

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
                        "<p>If you have any questions, please contact our support team.</p>" +
                        "</div>" +
                        "<div class='footer'>" +
                        "<p>This is an automated message from Pharma Aggregator. Please do not reply to this email.</p>" +
                        "<p>&copy; 2026 Pharma Aggregator. All rights reserved.</p>" +
                        "</div>" +
                        "</div>" +
                        "</body>" +
                        "</html>",
                action,
                coordinatorName != null ? coordinatorName : "Coordinator",
                sellerId != null ? sellerId : "N/A",
                sellerName,
                requestType,
                approvedBy,
                approvalTime
        );

        // Send HTML email using the new method
        emailService.sendHtmlMail(coordinatorEmail, subject, htmlBody);

        // Also send a plain text version as fallback (optional)
        // emailService.sendMail(coordinatorEmail, subject, "Your request has been approved...");
    }

    /**
     * Send rejection email to coordinator using HTML format
     */
    private void sendRejectionEmail(PendingSeller pendingSeller, String rejectionReason, String rejectedBy) {
        String coordinatorEmail = pendingSeller.getCoordinatorEmail();
        String coordinatorName = pendingSeller.getCoordinatorName();
        String sellerName = pendingSeller.getSellerName();
        String requestType = pendingSeller.getRequestType();

        if (coordinatorEmail == null || coordinatorEmail.isEmpty()) {
            log.warn("No coordinator email found for pending seller ID: {}", pendingSeller.getPendingSellerId());
            return;
        }

        String action = "CREATE".equals(requestType) ? "Creation" : "Update";
        String subject = String.format("Seller Profile %s Rejected - %s", action, sellerName);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        String rejectionTime = LocalDateTime.now().format(formatter);

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
                        "<p>If you need clarification, please contact our support team.</p>" +
                        "</div>" +
                        "<div class='footer'>" +
                        "<p>This is an automated message from Pharma Aggregator. Please do not reply to this email.</p>" +
                        "<p>&copy; 2026 Pharma Aggregator. All rights reserved.</p>" +
                        "</div>" +
                        "</div>" +
                        "</body>" +
                        "</html>",
                action,
                coordinatorName != null ? coordinatorName : "Coordinator",
                sellerName,
                requestType,
                rejectedBy,
                rejectionTime,
                rejectionReason != null ? rejectionReason : "No specific reason provided"
        );

        // Send HTML email using the new method
        emailService.sendHtmlMail(coordinatorEmail, subject, htmlBody);
    }

    /**
     * Get all pending seller requests and map to DTO
     * @return List of PendingSellerResponseDTO
     */
    public List<PendingSellerResponseDTO> getPendingRequests() {
        List<PendingSeller> pendingSellers = pendingSellerRepository.findByStatus("PENDING");

        return pendingSellers.stream()
                .map(this::mapToPendingResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get pending seller by ID
     */
//    public PendingSeller getPendingSellerById(Long pendingSellerId) {
//        return pendingSellerRepository.findById(pendingSellerId)
//                .orElseThrow(() -> new ResourceNotFoundException("Pending seller not found with id: " + pendingSellerId));
//    }

    /**
     * Map PendingSeller entity to PendingSellerResponseDTO
     * @param pendingSeller the entity to map
     * @return populated DTO
     */
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

    /**
     * Map PendingSellerDocument to DocumentDTO
     * @param document the document entity to map
     * @return populated DocumentDTO
     */
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

    private SellerResponseDTO mapToResponseDTO(PendingSeller savedPending) {
        SellerResponseDTO response = new SellerResponseDTO();

        response.setPendingSellerId(savedPending.getPendingSellerId());
        response.setMessage("Update request submitted successfully and pending admin approval");
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
                            .map(pt -> pt.getProductTypeId())
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

//    private SellerResponseDTO.AddressDTO convertToResponseAddress(SellerEditRequest.AddressDTO requestAddress) {
//        SellerResponseDTO.AddressDTO responseAddress = new SellerResponseDTO.AddressDTO();
//        responseAddress.setStateId(requestAddress.getStateId());
//        responseAddress.setDistrictId(requestAddress.getDistrictId());
//        responseAddress.setTalukaId(requestAddress.getTalukaId());
//        responseAddress.setCity(requestAddress.getCity());
//        responseAddress.setStreet(requestAddress.getStreet());
//        responseAddress.setBuildingNo(requestAddress.getBuildingNo());
//        responseAddress.setLandmark(requestAddress.getLandmark());
//        responseAddress.setPinCode(requestAddress.getPinCode());
//        return responseAddress;
//    }
//
//    private SellerResponseDTO.CoordinatorDTO convertToResponseCoordinator(SellerEditRequest.CoordinatorDTO requestCoordinator) {
//        SellerResponseDTO.CoordinatorDTO responseCoordinator = new SellerResponseDTO.CoordinatorDTO();
//        responseCoordinator.setName(requestCoordinator.getName());
//        responseCoordinator.setDesignation(requestCoordinator.getDesignation());
//        responseCoordinator.setEmail(requestCoordinator.getEmail());
//        responseCoordinator.setMobile(requestCoordinator.getMobile());
//        return responseCoordinator;
//    }
//
//    private SellerResponseDTO.BankDetailsDTO convertToResponseBankDetails(SellerEditRequest.BankDetailsDTO requestBankDetails) {
//        SellerResponseDTO.BankDetailsDTO responseBankDetails = new SellerResponseDTO.BankDetailsDTO();
//        responseBankDetails.setBankName(requestBankDetails.getBankName());
//        responseBankDetails.setBranch(requestBankDetails.getBranch());
//        responseBankDetails.setIfscCode(requestBankDetails.getIfscCode());
//        responseBankDetails.setAccountNumber(requestBankDetails.getAccountNumber());
//        responseBankDetails.setAccountHolderName(requestBankDetails.getAccountHolderName());
//        responseBankDetails.setBankDocumentFileUrl(requestBankDetails.getBankDocumentFileUrl());
//        return responseBankDetails;
//    }
//
//    private List<SellerResponseDTO.DocumentDTO> convertToResponseDocuments(List<SellerEditRequest.DocumentDTO> requestDocuments) {
//        return requestDocuments.stream()
//                .map(doc -> {
//                    SellerResponseDTO.DocumentDTO responseDoc = new SellerResponseDTO.DocumentDTO();
//                    responseDoc.setProductTypeId(doc.getProductTypeId());
//                    responseDoc.setProductTypeName(productTypeRepository.findById(doc.getProductTypeId())
//                            .orElse(new ProductTypeMaster()).getProductTypeName());
//                    responseDoc.setDocumentNumber(doc.getDocumentNumber());
//                    responseDoc.setDocumentFileUrl(doc.getDocumentFileUrl());
//                    responseDoc.setLicenseIssueDate(doc.getLicenseIssueDate());
//                    responseDoc.setLicenseExpiryDate(doc.getLicenseExpiryDate());
//                    responseDoc.setLicenseIssuingAuthority(doc.getLicenseIssuingAuthority());
//                    return responseDoc;
//                })
//                .collect(Collectors.toList());
//    }

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

    /**
     * Copies files from pendingsellers/{sellerId}/... to sellers/{sellerId}/...
     * Updates the URL fields on the pendingSeller entity IN PLACE before
     * updateSellerFromPending/createSellerFromPending reads them.
     * Old pendingsellers/ objects are deleted from S3 after successful copy.
     */
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
        // else: URL is already a sellers/ URL — untouched, copied as-is to main seller table

        // ── Bank document ─────────────────────────────────────────────────────
        if (isRealUrl(pending.getBankDocumentFileUrl()) && isPendingUrl(pending.getBankDocumentFileUrl())) {
            String ext       = extractExtension(pending.getBankDocumentFileUrl());
            String targetKey = String.format("sellers/%s/bankdocument/BANK_DETAILS_%s.%s", sellerId, now, ext);
            String newUrl    = s3Service.copyFile(s3Service.extractKeyFromUrl(pending.getBankDocumentFileUrl()), targetKey);
            s3Service.deleteFile(s3Service.extractKeyFromUrl(pending.getBankDocumentFileUrl()));
            pending.setBankDocumentFileUrl(newUrl);
            log.info("Bank document moved → {}", newUrl);
        }

        // ── Company Registration Certificate ──────────────────────────────────────────────────────────
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
                // else: already a sellers/ URL — no move needed
            }
        }
    }

    /** Only move files that were uploaded to the pending area */
    private boolean isPendingUrl(String url) {
        return url != null && url.contains("/pendingsellers/");
    }

    /** Returns true when a URL is a real S3 URL (not null/blank/PENDING) */
    private boolean isRealUrl(String url) {
        return url != null && !url.isBlank() && !"PENDING".equalsIgnoreCase(url.trim());
    }

    /** Extracts extension from a URL. Falls back to "bin". */
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

    /**
     * Get pending seller by ID and map to DTO
     * @param pendingSellerId the ID of the pending seller
     * @return PendingSellerResponseDTO with all details
     */
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

}