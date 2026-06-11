package com.example.pharmaaggregatorserver.service.serviceImpl.temp.seller;

import com.example.pharmaaggregatorserver.dto.admin.TempSellerAdminResponseDTO;
import com.example.pharmaaggregatorserver.dto.seller.OnSubmit.EmailRequestDTO;
import com.example.pharmaaggregatorserver.dto.seller.OnSubmit.EmailResponseDTO;
import com.example.pharmaaggregatorserver.dto.seller.*;
import com.example.pharmaaggregatorserver.entity.master.*;
import com.example.pharmaaggregatorserver.entity.seller.Seller;
import com.example.pharmaaggregatorserver.entity.temp.seller.*;
import com.example.pharmaaggregatorserver.exception.ApplicationException;
import com.example.pharmaaggregatorserver.exception.NotFoundException;
import com.example.pharmaaggregatorserver.repository.master.*;
import com.example.pharmaaggregatorserver.repository.seller.SellerRepository;
import com.example.pharmaaggregatorserver.repository.temp.seller.TempSellerBankDetailsRepository;
import com.example.pharmaaggregatorserver.repository.temp.seller.TempSellerDocumentRepository;
import com.example.pharmaaggregatorserver.repository.temp.seller.TempSellerRepository;
import com.example.pharmaaggregatorserver.service.S3Service;
import com.example.pharmaaggregatorserver.service.temp.seller.OnSubmit.IndependentEmailService;
import com.example.pharmaaggregatorserver.service.temp.seller.RequestIdGeneratorService;
import com.example.pharmaaggregatorserver.service.temp.seller.TempSellerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TempSellerServiceImpl implements TempSellerService {

    private final TempSellerRepository tempSellerRepository;
    private final ProductTypeMasterRepository productTypeMasterRepository;
    private final CompanyTypeMasterRepository companyTypeMasterRepository;
    private final SellerTypeMasterRepository sellerTypeMasterRepository;
    private final StateMasterRepository stateMasterRepository;
    private final DistrictMasterRepository districtMasterRepository;
    private final TalukaMasterRepository talukaMasterRepository;
    private final RequestIdGeneratorService requestIdGeneratorService;
    private final TempSellerDocumentRepository tempSellerDocumentRepository;
    private final TempSellerBankDetailsRepository tempSellerBankDetailsRepository;
    private final SellerRepository sellerRepository;
    private final S3Service s3Service;

    // Email service for sending confirmations
    private final IndependentEmailService independentEmailService;

    @Override
    @Transactional
    public TempSellerResponseDTO createTempSeller(TempSellerRequestDTO requestDTO) {
        String generatedRequestId = requestIdGeneratorService.generateNextRequestId();

        // ✅ VALIDATION: Phone and Email must be verified
//        if (!requestDTO.getCoordinator().isPhoneVerified()) {
//            throw new RuntimeException("Phone number must be verified before registration");
//        }
//        if (!requestDTO.getCoordinator().isEmailVerified()) {
//            throw new RuntimeException("Email must be verified before registration");
//        }

        // Check if phone or email already exists
//        if (tempSellerRepository.existsByPhone(requestDTO.getPhone())) {
//            throw new RuntimeException("Phone number already exists");
//        }
//        if (tempSellerRepository.existsByEmail(requestDTO.getEmail())) {
//            throw new RuntimeException("Email already exists");
//        }

        // Fetch master entities
        List<ProductTypeMaster> productType = productTypeMasterRepository.findAllById(requestDTO.getProductTypeId());

        CompanyTypeMaster companyType = companyTypeMasterRepository.findById(requestDTO.getCompanyTypeId())
                .orElseThrow(() -> new RuntimeException("Company type not found"));

        SellerTypeMaster sellerType = sellerTypeMasterRepository.findById(requestDTO.getSellerTypeId())
                .orElseThrow(() -> new RuntimeException("Seller type not found"));

        // Create main seller entity
        TempSeller seller = new TempSeller();
        seller.setSellerName(requestDTO.getSellerName());
        seller.setTempSellerRequestId(generatedRequestId);
        seller.setProductTypes(productType);
        seller.setCompanyType(companyType);
        seller.setSellerType(sellerType);
        seller.setPhone(requestDTO.getPhone());
        seller.setEmail(requestDTO.getEmail());
        seller.setWebsite(requestDTO.getWebsite());
        seller.setStatus("open");

        seller.setPhoneVerified(false);
        seller.setEmailVerified(false);
        seller.setGstNumber(requestDTO.getGstNumber());
        seller.setGstFileUrl(requestDTO.getGstFileUrl());
        seller.setTermsAccepted(requestDTO.isTermsAccepted());
        seller.setCompanyRegistrationCertificateUrl(requestDTO.getCompanyRegistrationCertificateUrl());
        seller.setCreatedBy("SYSTEM");
        seller.setUpdatedBy("SYSTEM");

        // Create address if provided
        if (requestDTO.getAddress() != null) {
            TempSellerAddress address = createAddress(requestDTO.getAddress(), seller);
            seller.setAddress(address);
        }

        // Create coordinator with verification status
//        if (requestDTO.getCoordinator() != null) {
//            TempSellerCoordinator coordinator = createCoordinator(requestDTO.getCoordinator(), seller);
//            // ✅ Set coordinator verification from DTO
//            coordinator.setPhoneVerified(requestDTO.getCoordinator().isPhoneVerified());
//            coordinator.setEmailVerified(requestDTO.getCoordinator().isEmailVerified());
//            seller.setCoordinator(coordinator);
//        }

        if (requestDTO.getCoordinator() != null) {
            TempSellerCoordinator coordinator = createCoordinator(requestDTO.getCoordinator(), seller);
            seller.setCoordinator(coordinator);
        }

        // Create bank details if provided
        if (requestDTO.getBankDetails() != null) {
            TempSellerBankDetails bankDetails = createBankDetails(requestDTO.getBankDetails(), seller);
            seller.setBankDetails(bankDetails);
        }

        // Create documents if provided
        if (requestDTO.getDocuments() != null && !requestDTO.getDocuments().isEmpty()) {
            for (TempSellerDocumentDTO docDTO : requestDTO.getDocuments()) {
                TempSellerDocument document = createDocument(docDTO, seller);
                seller.addDocument(document);
            }
        }

        // Save seller (cascade will save related entities)
        TempSeller savedSeller = tempSellerRepository.save(seller);

        // ============================================================
        // 🚀 AUTOMATICALLY SEND CONFIRMATION EMAIL ON SUCCESSFUL REGISTRATION
        // ============================================================
        sendConfirmationEmail(savedSeller, requestDTO);

        // Prepare and return response
        return mapToResponseDTO(savedSeller);
    }

    /**
     * Send confirmation email to coordinator after successful registration
     * WITH COMPLETE ADDRESS AND BANK DETAILS MAPPING
     */
    private void sendConfirmationEmail(TempSeller savedSeller, TempSellerRequestDTO requestDTO) {

        // Check if coordinator exists and has email
        if (requestDTO.getCoordinator() == null ||
                requestDTO.getCoordinator().getEmail() == null ||
                requestDTO.getCoordinator().getEmail().isEmpty()) {

            log.warn("⚠️ No coordinator email found for TempSeller ID: {}. Email not sent.",
                    savedSeller.getTempSellerId());
            return;
        }

        try {
            log.info("📧 Preparing confirmation email for Request ID: {}", savedSeller.getTempSellerRequestId());

            // Create EmailRequestDTO from requestDTO and savedSeller data
            EmailRequestDTO emailRequest = new EmailRequestDTO();

            // Basic Information
            emailRequest.setApplicationRequestId(savedSeller.getTempSellerRequestId());
            emailRequest.setSellerName(savedSeller.getSellerName());
            emailRequest.setSellerEmail(savedSeller.getEmail());
            emailRequest.setSellerPhone(savedSeller.getPhone());

            // Coordinator Information
            emailRequest.setCoordinatorName(requestDTO.getCoordinator().getName());
            emailRequest.setCoordinatorEmail(requestDTO.getCoordinator().getEmail());
            emailRequest.setCoordinatorMobile(requestDTO.getCoordinator().getMobile());
            emailRequest.setCoordinatorDesignation(requestDTO.getCoordinator().getDesignation());

            // ============================================================
            // 🏢 ADDRESS INFORMATION MAPPING
            // ============================================================
            if (requestDTO.getAddress() != null) {
                TempSellerAddressDTO addr = requestDTO.getAddress();
                emailRequest.setAddressCity(addr.getCity());
                emailRequest.setAddressStreet(addr.getStreet());
                emailRequest.setAddressBuildingNo(addr.getBuildingNo());
                emailRequest.setAddressLandmark(addr.getLandmark());
                emailRequest.setAddressPinCode(addr.getPinCode());

                // Fetch and set state, district, taluka names if IDs are provided
                try {
                    if (addr.getStateId() != null) {
                        StateMaster state = stateMasterRepository.findById(addr.getStateId()).orElse(null);
                        if (state != null) {
                            emailRequest.setAddressState(state.getStateName());
                        }
                    }
                    if (addr.getDistrictId() != null) {
                        DistrictMaster district = districtMasterRepository.findById(addr.getDistrictId()).orElse(null);
                        if (district != null) {
                            emailRequest.setAddressDistrict(district.getDistrictName());
                        }
                    }
                    if (addr.getTalukaId() != null) {
                        TalukaMaster taluka = talukaMasterRepository.findById(addr.getTalukaId()).orElse(null);
                        if (taluka != null) {
                            emailRequest.setAddressTaluka(taluka.getTalukaName());
                        }
                    }
                } catch (Exception e) {
                    log.warn("⚠️ Error fetching location names: {}", e.getMessage());
                }

                log.info("📍 Address mapped: {}, {}", addr.getCity(), addr.getPinCode());
            }

            // ============================================================
            // 🏦 BANK DETAILS INFORMATION MAPPING
            // ============================================================
            if (requestDTO.getBankDetails() != null) {
                TempSellerBankDetailsDTO bank = requestDTO.getBankDetails();
                emailRequest.setBankName(bank.getBankName());
                emailRequest.setBankBranch(bank.getBranch());
                emailRequest.setBankIfscCode(bank.getIfscCode());
                emailRequest.setBankAccountNumber(bank.getAccountNumber());
                emailRequest.setBankAccountHolderName(bank.getAccountHolderName());

                log.info("🏦 Bank details mapped for: {}", bank.getBankName());
            }

            // ============================================================
            // 📄 DOCUMENT INFORMATION (first document)
            // ============================================================
            if (requestDTO.getDocuments() != null && !requestDTO.getDocuments().isEmpty()) {
                TempSellerDocumentDTO firstDoc = requestDTO.getDocuments().get(0);
                emailRequest.setGstNumber(savedSeller.getGstNumber());
                emailRequest.setDocumentNumber(firstDoc.getDocumentNumber());

                log.info("📄 Document details mapped");
            }

            // Log email details
            log.info("📧 Sending email to: {}", emailRequest.getCoordinatorEmail());
            log.info("📋 Application Request ID: {}", emailRequest.getApplicationRequestId());
            log.info("🏢 Seller: {}", emailRequest.getSellerName());
            log.info("📍 Address: {}, {}",
                    emailRequest.getAddressCity() != null ? emailRequest.getAddressCity() : "Not Provided",
                    emailRequest.getAddressPinCode() != null ? emailRequest.getAddressPinCode() : "Not Provided");

            // Call the independent email service
            EmailResponseDTO emailResponse = independentEmailService.sendApplicationConfirmationEmail(emailRequest);

            // Log the result
            if (emailResponse.isSuccess()) {
                log.info("✅ Confirmation email sent successfully to: {}", emailRequest.getCoordinatorEmail());
            } else {
                log.error("❌ Failed to send confirmation email: {}", emailResponse.getMessage());
            }

        } catch (Exception e) {
            // Log error but don't throw exception - email failure should not rollback seller creation
            log.error("❌ Error sending confirmation email for TempSeller ID: {} - Error: {}",
                    savedSeller.getTempSellerId(), e.getMessage(), e);
        }
    }

    /**
     * Create address entity from DTO
     */
    private TempSellerAddress createAddress(TempSellerAddressDTO addressDTO, TempSeller seller) {
        StateMaster state = stateMasterRepository.findById(addressDTO.getStateId())
                .orElseThrow(() -> new RuntimeException("State not found"));
        DistrictMaster district = districtMasterRepository.findById(addressDTO.getDistrictId())
                .orElseThrow(() -> new RuntimeException("District not found"));
        TalukaMaster taluka = talukaMasterRepository.findById(addressDTO.getTalukaId())
                .orElseThrow(() -> new RuntimeException("Taluka not found"));

        TempSellerAddress address = new TempSellerAddress();
        address.setSeller(seller);
        address.setState(state);
        address.setDistrict(district);
        address.setTaluka(taluka);
        address.setCity(addressDTO.getCity());
        address.setStreet(addressDTO.getStreet());
        address.setBuildingNo(addressDTO.getBuildingNo());
        address.setLandmark(addressDTO.getLandmark());
        address.setPinCode(addressDTO.getPinCode());
        address.setCreatedBy("SYSTEM");
        address.setUpdatedBy("SYSTEM");

        return address;
    }

    /**
     * Create coordinator entity from DTO
     */
    private TempSellerCoordinator createCoordinator(TempSellerCoordinatorDTO coordinatorDTO, TempSeller seller) {
        TempSellerCoordinator coordinator = new TempSellerCoordinator();
        coordinator.setSeller(seller);
        coordinator.setName(coordinatorDTO.getName());
        coordinator.setDesignation(coordinatorDTO.getDesignation());
        coordinator.setEmail(coordinatorDTO.getEmail());
        coordinator.setMobile(coordinatorDTO.getMobile());
        coordinator.setEmailVerified(false);
        coordinator.setPhoneVerified(false);
        coordinator.setCreatedBy("SYSTEM");
        coordinator.setUpdatedBy("SYSTEM");

        return coordinator;
    }

    /**
     * Create bank details entity from DTO
     */
    private TempSellerBankDetails createBankDetails(TempSellerBankDetailsDTO bankDetailsDTO, TempSeller seller) {
        TempSellerBankDetails bankDetails = new TempSellerBankDetails();
        bankDetails.setSeller(seller);
        bankDetails.setBankName(bankDetailsDTO.getBankName());
        bankDetails.setBranch(bankDetailsDTO.getBranch());
        bankDetails.setIfscCode(bankDetailsDTO.getIfscCode());
        bankDetails.setAccountNumber(bankDetailsDTO.getAccountNumber());
        bankDetails.setAccountHolderName(bankDetailsDTO.getAccountHolderName());
        bankDetails.setBankDocumentFileUrl(bankDetailsDTO.getBankDocumentFileUrl());
        bankDetails.setCreatedBy("SYSTEM");
        bankDetails.setUpdatedBy("SYSTEM");

        return bankDetails;
    }

    /**
     * Create document entity from DTO
     */
    private TempSellerDocument createDocument(TempSellerDocumentDTO docDTO, TempSeller seller) {
        ProductTypeMaster productType = productTypeMasterRepository.findById(docDTO.getProductTypeId())
                .orElseThrow(() -> new RuntimeException("Product type not found for document"));

        TempSellerDocument document = new TempSellerDocument();
        document.setSeller(seller);
        document.setProductTypes(productType);
        document.setDocumentNumber(docDTO.getDocumentNumber());
        document.setDocumentFileUrl(docDTO.getDocumentFileUrl());
        document.setLicenseIssueDate(docDTO.getLicenseIssueDate());
        document.setLicenseExpiryDate(docDTO.getLicenseExpiryDate());
        document.setLicenseIssuingAuthority(docDTO.getLicenseIssuingAuthority());
        document.setCreatedBy("SYSTEM");
        document.setUpdatedBy("SYSTEM");

        return document;
    }

    /**
     * Map TempSeller entity to Response DTO
     */
    private TempSellerResponseDTO mapToResponseDTO(TempSeller seller) {
        TempSellerResponseDTO responseDTO = new TempSellerResponseDTO();
        responseDTO.setTempSellerId(seller.getTempSellerId());
        responseDTO.setSellerName(seller.getSellerName());
        responseDTO.setSellerRequestId(seller.getTempSellerRequestId());
        responseDTO.setPhone(seller.getPhone());
        responseDTO.setEmail(seller.getEmail());
        responseDTO.setStatus(seller.getStatus());
        responseDTO.setCreatedAt(seller.getCreatedAt());

        // Map saved documents → documentId + licenseName for the upload step
        if (seller.getDocuments() != null) {
            List<TempSellerResponseDTO.DocumentInfo> docInfos = seller.getDocuments().stream()
                    .map(doc -> {
                        TempSellerResponseDTO.DocumentInfo info = new TempSellerResponseDTO.DocumentInfo();
                        info.setDocumentId(doc.getDocumentsId());
                        info.setLicenseName(doc.getProductTypes().getProductTypeName()); // adjust getter as per your entity
                        return info;
                    })
                    .toList();
            responseDTO.setDocuments(docInfos);
        }

        return responseDTO;
    }

    /**
     * Get all temp sellers
     */
    @Override
    public List<TempSellerAdminResponseDTO> getALLTempSellers() {
        List<TempSeller> tempSellers = tempSellerRepository.findAll();

        if (tempSellers.isEmpty()) {
            return List.of();
        }

        List<TempSellerAdminResponseDTO> dtos = new ArrayList<>();
        tempSellers.forEach(tempSeller -> {
            TempSellerAdminResponseDTO dto = new TempSellerAdminResponseDTO();
            dto.setTempSellerId(tempSeller.getTempSellerId());
            dto.setTempSellerRequestId(tempSeller.getTempSellerRequestId());
            dto.setTempSellerName(tempSeller.getSellerName());
            dto.setTempSellerEmail(tempSeller.getEmail());
            dto.setCreatedAt(tempSeller.getCreatedAt());
            dto.setStatus(tempSeller.getStatus());
            dtos.add(dto);
        });
        return dtos;
    }

    /**
     * Find temp seller by ID
     */
    @Override
    public TempSeller findById(Long id) {
        return tempSellerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("TempSeller not found for id: " + id));
    }

    @Override
    @Transactional
    public void updateGstVerification(Long tempSellerId, boolean isGstVerified) {
        TempSeller seller = tempSellerRepository.findById(tempSellerId)
                .orElseThrow(() -> new NotFoundException("TempSeller not found for id: " + tempSellerId));

        seller.setGstVerified(isGstVerified);
        log.info("GST verified: " + seller.isGstVerified());
        log.info("From API GST verified: " + isGstVerified);
        tempSellerRepository.save(seller);
    }

    @Override
    @Transactional
    public void updateCompanyRegistrationCertificateVerification(Long tempSellerId, boolean isCompanyRegistrationCertificateVerified) {
        TempSeller seller = tempSellerRepository.findById(tempSellerId)
                .orElseThrow(() -> new NotFoundException("TempSeller not found for id: " + tempSellerId));

        seller.setCompanyRegistrationCertificateVerified(isCompanyRegistrationCertificateVerified);
        log.info("Company Registration Certificate verified: " + seller.isCompanyRegistrationCertificateVerified());
        log.info("From API Company Registration Certificate verified: " + isCompanyRegistrationCertificateVerified);
        tempSellerRepository.save(seller);
    }

    @Override
    @Transactional
    public void updateDocumentVerification(Long tempSellerId, Long documentId, boolean isDocumentVerified) {
        // Confirm seller exists
        if (!tempSellerRepository.existsById(tempSellerId)) {
            throw new NotFoundException("TempSeller not found for id: " + tempSellerId);
        }

        TempSellerDocument doc = tempSellerDocumentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document not found for id: " + documentId));

        // Guard: document must belong to this seller
        if (!doc.getSeller().getTempSellerId().equals(tempSellerId)) {
            throw new ApplicationException("Document id=" + documentId + " does not belong to sellerId=" + tempSellerId);
        }

        doc.setDocumentVerified(isDocumentVerified);
        tempSellerDocumentRepository.save(doc);
    }

    @Override
    @Transactional
    public void updateBankDocumentVerification(Long tempSellerId, boolean isBankDocumentVerified) {
        TempSellerBankDetails bankDetails = tempSellerBankDetailsRepository
                .findBySeller_TempSellerId(tempSellerId)
                .orElseThrow(() -> new NotFoundException("Bank details not found for sellerId: " + tempSellerId));

        bankDetails.setBankDocumentVerified(isBankDocumentVerified);
        tempSellerBankDetailsRepository.save(bankDetails);
    }

    @Override
    public void deleteTempSeller(Long tempSellerId) {
        TempSeller tempSeller = tempSellerRepository.findById(tempSellerId)
                .orElseThrow(() -> new NotFoundException("TempSeller not found for id: " + tempSellerId));

        deleteTempSellerS3Files(tempSeller);
        tempSellerRepository.delete(tempSeller);
//        log.info("TempSeller deleted with id: {}", tempSellerId);
    }

    @Override
    public void deleteBothSellerAndTempSeller(Long tempSellerId) {
        TempSeller tempSeller = tempSellerRepository.findById(tempSellerId)
                .orElseThrow(() -> new NotFoundException("TempSeller not found for id: " + tempSellerId));

        // If the TempSeller was approved, also delete the corresponding Seller
        if ("APPROVED".equals(tempSeller.getStatus())) {
            sellerRepository.findByEmail(tempSeller.getEmail())
                    .ifPresent(seller -> {
                        log.info("Deleting approved Seller with email: {}", tempSeller.getEmail());
                        deleteSellerS3Files(seller);
                        sellerRepository.delete(seller);
                    });
        }

        deleteTempSellerS3Files(tempSeller);
        tempSellerRepository.delete(tempSeller);
//        log.info("TempSeller deleted with id: {}", tempSellerId);
    }

    @Override
    @Transactional
    public TempSellerResponseDTO updateTempSeller(Long tempSellerId, TempSellerRequestDTO requestDTO) {
        TempSeller seller = tempSellerRepository.findById(tempSellerId)
                .orElseThrow(() -> new NotFoundException("TempSeller not found for id: " + tempSellerId));

        if (seller.getStatus().equalsIgnoreCase("APPROVED")) {
            throw new ApplicationException("You are not allowed to update this application because it is already approved.");
        }

        if (seller.getStatus().equalsIgnoreCase("REJECTED")) {
            throw new ApplicationException("You are not allowed to update this application because it is already rejected.");
        }

        if (seller.getStatus().equalsIgnoreCase("RESUBMITTED") || seller.getStatus().equalsIgnoreCase("OPEN")) {
            throw new ApplicationException("You are not allowed to update this application because it is currently under admin review.");
        }

        List<ProductTypeMaster> productType = productTypeMasterRepository.findAllById(requestDTO.getProductTypeId());

        CompanyTypeMaster companyType = companyTypeMasterRepository.findById(requestDTO.getCompanyTypeId())
                .orElseThrow(() -> new RuntimeException("Company type not found"));

        SellerTypeMaster sellerType = sellerTypeMasterRepository.findById(requestDTO.getSellerTypeId())
                .orElseThrow(() -> new RuntimeException("Seller type not found"));

        // ── Core fields ───────────────────────────────────────────────────────────
        seller.setSellerName(requestDTO.getSellerName());
        seller.setProductTypes(productType);
        seller.setCompanyType(companyType);
        seller.setSellerType(sellerType);
        seller.setPhone(requestDTO.getPhone());
        seller.setEmail(requestDTO.getEmail());
        seller.setWebsite(requestDTO.getWebsite());
        seller.setStatus("RESUBMITTED");
        seller.setGstNumber(requestDTO.getGstNumber());
        seller.setUpdatedBy("SYSTEM");

        // ── Address ───────────────────────────────────────────────────────────────
        if (requestDTO.getAddress() != null) {
            if (seller.getAddress() == null) {
                seller.setAddress(createAddress(requestDTO.getAddress(), seller));
            } else {
                TempSellerAddress address = seller.getAddress();

                if (!Objects.equals(address.getState().getStateId(), requestDTO.getAddress().getStateId())) {
                    StateMaster state = stateMasterRepository.findById(requestDTO.getAddress().getStateId())
                            .orElseThrow(() -> new RuntimeException("State not found"));
                    address.setState(state);
                }
                if (!Objects.equals(address.getDistrict().getDistrictId(), requestDTO.getAddress().getDistrictId())) {
                    DistrictMaster district = districtMasterRepository.findById(requestDTO.getAddress().getDistrictId())
                            .orElseThrow(() -> new RuntimeException("District not found"));
                    address.setDistrict(district);
                }
                if (!Objects.equals(address.getTaluka().getTalukaId(), requestDTO.getAddress().getTalukaId())) {
                    TalukaMaster taluka = talukaMasterRepository.findById(requestDTO.getAddress().getTalukaId())
                            .orElseThrow(() -> new RuntimeException("Taluka not found"));
                    address.setTaluka(taluka);
                }

                address.setCity(requestDTO.getAddress().getCity());
                address.setStreet(requestDTO.getAddress().getStreet());
                address.setBuildingNo(requestDTO.getAddress().getBuildingNo());
                address.setLandmark(requestDTO.getAddress().getLandmark());
                address.setPinCode(requestDTO.getAddress().getPinCode());
                address.setUpdatedBy("SYSTEM");
            }
        }

        // ── Coordinator ───────────────────────────────────────────────────────────
        if (requestDTO.getCoordinator() != null) {
            if (seller.getCoordinator() == null) {
                seller.setCoordinator(createCoordinator(requestDTO.getCoordinator(), seller));
            } else {
                TempSellerCoordinator coordinator = seller.getCoordinator();
                TempSellerCoordinatorDTO coordinatorDTO = requestDTO.getCoordinator();
                coordinator.setName(coordinatorDTO.getName());
                coordinator.setDesignation(coordinatorDTO.getDesignation());
                coordinator.setEmail(coordinatorDTO.getEmail());
                coordinator.setEmailVerified(coordinatorDTO.isEmailVerified());
                coordinator.setMobile(coordinatorDTO.getMobile());
                coordinator.setPhoneVerified(coordinatorDTO.isPhoneVerified());
                coordinator.setUpdatedBy("SYSTEM");
            }
        }

        // ── Bank Details ──────────────────────────────────────────────────────────
        if (requestDTO.getBankDetails() != null) {
            if (seller.getBankDetails() == null) {
                seller.setBankDetails(createBankDetails(requestDTO.getBankDetails(), seller));
            } else {
                TempSellerBankDetails bank = seller.getBankDetails();
                TempSellerBankDetailsDTO bankDTO = requestDTO.getBankDetails();

                bank.setBankName(bankDTO.getBankName());
                bank.setBranch(bankDTO.getBranch());
                bank.setIfscCode(bankDTO.getIfscCode());
                bank.setAccountNumber(bankDTO.getAccountNumber());
                bank.setAccountHolderName(bankDTO.getAccountHolderName());
                bank.setUpdatedBy("SYSTEM");
            }
        }

        // ── Documents ─────────────────────────────────────────────────────────────
        if (requestDTO.getDocuments() != null) {

            // Build a map of existing documents by productTypeId for quick lookup
            Map<Long, TempSellerDocument> existingDocMap = seller.getDocuments().stream()
                    .collect(Collectors.toMap(
                            doc -> doc.getProductTypes().getProductTypeId(),
                            doc -> doc
                    ));

            // Build a set of incoming productTypeIds
            Set<Long> incomingProductTypeIds = requestDTO.getDocuments().stream()
                    .map(TempSellerDocumentDTO::getProductTypeId)
                    .collect(Collectors.toSet());

            // 1. DELETE documents whose productTypeId is no longer in the request + delete S3 file
            existingDocMap.forEach((productTypeId, existingDoc) -> {
                if (!incomingProductTypeIds.contains(productTypeId)) {
                    deleteS3File(existingDoc.getDocumentFileUrl());
                    seller.getDocuments().remove(existingDoc); // orphanRemoval will delete from DB
                }
            });

            // 2. UPDATE existing or ADD new documents
            for (TempSellerDocumentDTO docDTO : requestDTO.getDocuments()) {
                TempSellerDocument existingDoc = existingDocMap.get(docDTO.getProductTypeId());

                if (existingDoc != null) {
                    // UPDATE existing document row
                    existingDoc.setDocumentNumber(docDTO.getDocumentNumber());
                    existingDoc.setLicenseIssueDate(docDTO.getLicenseIssueDate());
                    existingDoc.setLicenseExpiryDate(docDTO.getLicenseExpiryDate());
                    existingDoc.setLicenseIssuingAuthority(docDTO.getLicenseIssuingAuthority());
                    existingDoc.setUpdatedBy("SYSTEM");
                } else {
                    // ADD new document row
                    seller.addDocument(createDocument(docDTO, seller));
                }
            }
        }

        TempSeller savedSeller = tempSellerRepository.save(seller);
        return mapToResponseDTO(savedSeller);
    }

    // ─── Private Helpers ──────────────────────────────────────────────────────────

    private void deleteTempSellerS3Files(TempSeller tempSeller) {
        deleteS3File(tempSeller.getSellerImageUrl());
        deleteS3File(tempSeller.getGstFileUrl());
        deleteS3File(tempSeller.getCompanyRegistrationCertificateUrl());

        if (tempSeller.getBankDetails() != null) {
            deleteS3File(tempSeller.getBankDetails().getBankDocumentFileUrl());
        }

        if (tempSeller.getDocuments() != null) {
            tempSeller.getDocuments()
                    .forEach(doc -> deleteS3File(doc.getDocumentFileUrl()));
        }
    }

    private void deleteSellerS3Files(Seller seller) {
        deleteS3File(seller.getSellerImageUrl());

        if (seller.getSellerGST() != null) {
            deleteS3File(seller.getSellerGST().getGstFileUrl());
        }

        if (seller.getBankDetails() != null) {
            deleteS3File(seller.getBankDetails().getBankDocumentFileUrl());
        }

        if (seller.getDocuments() != null) {
            seller.getDocuments()
                    .forEach(doc -> deleteS3File(doc.getDocumentFileUrl()));
        }
    }

    private void deleteS3File(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) return;
        try {
            String key = s3Service.extractKeyFromUrl(fileUrl);
            s3Service.deleteFile(key);
//            log.info("Deleted S3 file: {}", key);
        } catch (Exception e) {
            // Log but don't fail the delete — the DB record should still be removed
            log.warn("Could not delete S3 file for URL: {}. Reason: {}", fileUrl, e.getMessage());
        }
    }
}