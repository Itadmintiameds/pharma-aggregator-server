package com.example.pharmaaggregatorserver.service.serviceImpl.temp.seller;

import com.example.pharmaaggregatorserver.dto.admin.TempSellerAdminResponseDTO;
import com.example.pharmaaggregatorserver.dto.seller.OnSubmit.EmailRequestDTO;
import com.example.pharmaaggregatorserver.dto.seller.OnSubmit.EmailResponseDTO;
import com.example.pharmaaggregatorserver.dto.seller.*;
import com.example.pharmaaggregatorserver.entity.master.*;
import com.example.pharmaaggregatorserver.entity.temp.seller.*;
import com.example.pharmaaggregatorserver.exception.ApplicationException;
import com.example.pharmaaggregatorserver.exception.NotFoundException;
import com.example.pharmaaggregatorserver.repository.master.*;
import com.example.pharmaaggregatorserver.repository.seller.SellerRepository;
import com.example.pharmaaggregatorserver.repository.temp.seller.TempSellerBankDetailsRepository;
import com.example.pharmaaggregatorserver.repository.temp.seller.TempSellerDocumentRepository;
import com.example.pharmaaggregatorserver.repository.temp.seller.TempSellerRepository;
import com.example.pharmaaggregatorserver.service.temp.seller.OnSubmit.IndependentEmailService;
import com.example.pharmaaggregatorserver.service.temp.seller.RequestIdGeneratorService;
import com.example.pharmaaggregatorserver.service.temp.seller.TempSellerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

    // Email service for sending confirmations
    private final IndependentEmailService independentEmailService;

    @Override
    @Transactional
    public TempSellerResponseDTO createTempSeller(TempSellerRequestDTO requestDTO) {
        String generatedRequestId = requestIdGeneratorService.generateNextRequestId();

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
        seller.setCreatedBy("SYSTEM");
        seller.setUpdatedBy("SYSTEM");

        // Create address if provided
        if (requestDTO.getAddress() != null) {
            TempSellerAddress address = createAddress(requestDTO.getAddress(), seller);
            seller.setAddress(address);
        }

        // Create coordinator if provided
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
        tempSellerRepository.deleteById(tempSellerId);
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
                        sellerRepository.delete(seller);
                    });
        }

        tempSellerRepository.deleteById(tempSellerId);
        log.info("TempSeller deleted with id: {}", tempSellerId);
    }
}