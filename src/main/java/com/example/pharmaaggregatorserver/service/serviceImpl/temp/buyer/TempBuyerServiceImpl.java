package com.example.pharmaaggregatorserver.service.serviceImpl.temp.buyer;

import com.example.pharmaaggregatorserver.dto.admin.TempBuyerAdminResponseDTO;
import com.example.pharmaaggregatorserver.dto.buyer.TempBuyerResponseDTO;
import com.example.pharmaaggregatorserver.dto.temp.buyer.TempBuyerAddressDTO;
import com.example.pharmaaggregatorserver.dto.temp.buyer.TempBuyerContactDTO;
import com.example.pharmaaggregatorserver.dto.temp.buyer.TempBuyerDocumentDto;
import com.example.pharmaaggregatorserver.dto.temp.buyer.TempBuyerDraftRequestDTO;
import com.example.pharmaaggregatorserver.dto.temp.buyer.TempBuyerRequestDTO;
import com.example.pharmaaggregatorserver.entity.buyer.Buyer;
import com.example.pharmaaggregatorserver.entity.buyer.BuyerUser;
import com.example.pharmaaggregatorserver.entity.master.BuyerTypeMaster;
import com.example.pharmaaggregatorserver.entity.master.DistrictMaster;
import com.example.pharmaaggregatorserver.entity.master.DocumentTypeMaster;
import com.example.pharmaaggregatorserver.entity.master.StateMaster;
import com.example.pharmaaggregatorserver.entity.master.TalukaMaster;
import com.example.pharmaaggregatorserver.entity.temp.buyer.TempBuyer;
import com.example.pharmaaggregatorserver.entity.temp.buyer.TempBuyerAddress;
import com.example.pharmaaggregatorserver.entity.temp.buyer.TempBuyerContact;
import com.example.pharmaaggregatorserver.entity.temp.buyer.TempBuyerDocument;
import com.example.pharmaaggregatorserver.entity.temp.buyer.TempBuyerStatus;
import com.example.pharmaaggregatorserver.exception.ApplicationException;
import com.example.pharmaaggregatorserver.exception.NotFoundException;
import com.example.pharmaaggregatorserver.repository.buyer.BuyerRepository;
import com.example.pharmaaggregatorserver.repository.buyer.BuyerUserRepository;
import com.example.pharmaaggregatorserver.repository.master.BuyerTypeMasterRepository;
import com.example.pharmaaggregatorserver.repository.master.DistrictMasterRepository;
import com.example.pharmaaggregatorserver.repository.master.DocumentTypeMasterRepository;
import com.example.pharmaaggregatorserver.repository.master.StateMasterRepository;
import com.example.pharmaaggregatorserver.repository.master.TalukaMasterRepository;
import com.example.pharmaaggregatorserver.repository.temp.buyer.TempBuyerDocumentRepository;
import com.example.pharmaaggregatorserver.repository.temp.buyer.TempBuyerRepository;
import com.example.pharmaaggregatorserver.service.S3Service;
import com.example.pharmaaggregatorserver.service.temp.buyer.BuyerRequestIdGeneratorService;
import com.example.pharmaaggregatorserver.service.temp.buyer.TempBuyerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TempBuyerServiceImpl implements TempBuyerService {

    private static final String PENDING = "PENDING";

    private final TempBuyerRepository tempBuyerRepository;
    private final BuyerTypeMasterRepository buyerTypeMasterRepository;
    private final StateMasterRepository stateMasterRepository;
    private final DistrictMasterRepository districtMasterRepository;
    private final TalukaMasterRepository talukaMasterRepository;
    private final DocumentTypeMasterRepository documentTypeMasterRepository;
    private final TempBuyerDocumentRepository tempBuyerDocumentRepository;
    private final BuyerRepository buyerRepository;
    private final BuyerUserRepository buyerUserRepository;
    private final BuyerRequestIdGeneratorService requestIdGeneratorService;
    private final S3Service s3Service;

    @Override
    @Transactional
    public TempBuyerResponseDTO createTempBuyer(TempBuyerRequestDTO requestDTO) {

        BuyerTypeMaster buyerType = buyerTypeMasterRepository.findById(requestDTO.getBuyerTypeId())
                .orElseThrow(() -> new NotFoundException("Buyer type not found for id: " + requestDTO.getBuyerTypeId()));

        validatePanRequiredWhenNoGst(requestDTO.getGstNumber(), requestDTO.getPanNumber());

        TempBuyer buyer = new TempBuyer();
        buyer.setTempBuyerRequestId(requestIdGeneratorService.generateNextRequestId());
        buyer.setOrganizationName(requestDTO.getOrganizationName());
        buyer.setOrgLogoUrl(requestDTO.getOrgLogoUrl());
        buyer.setBuyerType(buyerType);
        buyer.setGstNumber(requestDTO.getGstNumber());
        buyer.setPanNumber(requestDTO.getPanNumber());
        buyer.setTermsAccepted(requestDTO.isTermsAccepted());
        buyer.setStatus(TempBuyerStatus.SUBMITTED);
        buyer.setCreatedBy("SYSTEM");
        buyer.setUpdatedBy("SYSTEM");

        if (requestDTO.getBuyerUserId() != null) {
            BuyerUser user = buyerUserRepository.findById(requestDTO.getBuyerUserId())
                    .orElseThrow(() -> new NotFoundException("BuyerUser not found for id: " + requestDTO.getBuyerUserId()));
            buyer.setUser(user);
        }

        if (requestDTO.getAddress() != null) {
            buyer.setAddress(createAddress(requestDTO.getAddress(), buyer));
        }

        if (requestDTO.getContact() != null) {
            buyer.setContact(createContact(requestDTO.getContact(), buyer));
        }

        // Auto-create the single mandatory document placeholder row for this
        // buyer type. If the request already supplied a matching document
        // entry (same documentTypeId), use its details; otherwise leave a
        // "PENDING" placeholder to be filled in later via the draft/upload
        // flow.
        TempBuyerDocumentDto matching = findMatchingDocument(requestDTO.getDocuments(), buyerType);
        buyer.addDocument(createMandatoryDocument(buyerType, matching));

        TempBuyer saved = tempBuyerRepository.save(buyer);
        return mapToResponseDTO(saved);
    }

    private TempBuyerDocumentDto findMatchingDocument(List<TempBuyerDocumentDto> documents, BuyerTypeMaster buyerType) {
        if (documents == null || documents.isEmpty() || buyerType.getMandatoryDocumentTypeId() == null) {
            return null;
        }
        Long mandatoryDocTypeId = buyerType.getMandatoryDocumentTypeId().getDocumentTypeId();
        return documents.stream()
                .filter(d -> d.getDocumentTypeId() != null && d.getDocumentTypeId().equals(mandatoryDocTypeId))
                .findFirst()
                .orElse(documents.get(0));
    }

    private TempBuyerDocument createMandatoryDocument(BuyerTypeMaster buyerType, TempBuyerDocumentDto docDTO) {
        TempBuyerDocument document = new TempBuyerDocument();
        document.setDocumentType(buyerType.getMandatoryDocumentTypeId());
        document.setDocumentNumber(docDTO != null && !isBlank(docDTO.getDocumentNumber()) ? docDTO.getDocumentNumber() : PENDING);
        document.setDocumentFileUrl(docDTO != null && !isBlank(docDTO.getDocumentFileUrl()) ? docDTO.getDocumentFileUrl() : PENDING);
        if (docDTO != null) {
            document.setLicenseIssueDate(docDTO.getLicenseIssueDate());
            document.setLicenseExpiryDate(docDTO.getLicenseExpiryDate());
            document.setLicenseIssuingAuthority(docDTO.getLicenseIssuingAuthority());
        }
        document.setCreatedBy("SYSTEM");
        document.setUpdatedBy("SYSTEM");
        return document;
    }

    private TempBuyerAddress createAddress(TempBuyerAddressDTO dto, TempBuyer buyer) {
        StateMaster state = stateMasterRepository.findById(dto.getStateId())
                .orElseThrow(() -> new NotFoundException("State not found"));
        DistrictMaster district = districtMasterRepository.findById(dto.getDistrictId())
                .orElseThrow(() -> new NotFoundException("District not found"));
        TalukaMaster taluka = talukaMasterRepository.findById(dto.getTalukaId())
                .orElseThrow(() -> new NotFoundException("Taluka not found"));

        TempBuyerAddress address = new TempBuyerAddress();
        address.setBuyer(buyer);
        address.setState(state);
        address.setDistrict(district);
        address.setTaluka(taluka);
        address.setCity(dto.getCity());
        address.setStreet(dto.getStreet());
        address.setBuildingNo(dto.getBuildingNo());
        address.setLandmark(dto.getLandmark());
        address.setPinCode(dto.getPinCode());
        address.setCreatedBy("SYSTEM");
        address.setUpdatedBy("SYSTEM");
        return address;
    }

    private TempBuyerContact createContact(TempBuyerContactDTO dto, TempBuyer buyer) {
        TempBuyerContact contact = new TempBuyerContact();
        contact.setBuyer(buyer);
        contact.setName(dto.getName());
        contact.setDesignation(dto.getDesignation());
        contact.setEmail(dto.getEmail());
        contact.setMobile(dto.getMobile());
        contact.setEmailVerified(false);
        contact.setPhoneVerified(false);
        contact.setCreatedBy("SYSTEM");
        contact.setUpdatedBy("SYSTEM");
        return contact;
    }

    private TempBuyerResponseDTO mapToResponseDTO(TempBuyer buyer) {
        TempBuyerResponseDTO dto = new TempBuyerResponseDTO();
        dto.setTempBuyerId(buyer.getTempBuyerId());
        dto.setOrganizationName(buyer.getOrganizationName());
        dto.setTempBuyerRequestId(buyer.getTempBuyerRequestId());
        dto.setStatus(buyer.getStatus());
        dto.setCreatedAt(buyer.getCreatedAt());

        if (buyer.getDocuments() != null) {
            List<TempBuyerResponseDTO.DocumentInfo> docInfos = buyer.getDocuments().stream()
                    .map(doc -> {
                        TempBuyerResponseDTO.DocumentInfo info = new TempBuyerResponseDTO.DocumentInfo();
                        info.setDocumentId(doc.getTempBuyerDocumentId());
                        info.setDocumentTypeName(doc.getDocumentType() != null ? doc.getDocumentType().getDocumentTypeName() : null);
                        return info;
                    })
                    .toList();
            dto.setDocuments(docInfos);
        }
        return dto;
    }

    @Override
    public List<TempBuyerAdminResponseDTO> getAllTempBuyers() {
        List<TempBuyer> tempBuyers = tempBuyerRepository.findAll();
        if (tempBuyers.isEmpty()) {
            return List.of();
        }
        List<TempBuyerAdminResponseDTO> dtos = new ArrayList<>();
        tempBuyers.forEach(tempBuyer -> {
            TempBuyerAdminResponseDTO dto = new TempBuyerAdminResponseDTO();
            dto.setTempBuyerId(tempBuyer.getTempBuyerId());
            dto.setTempBuyerRequestId(tempBuyer.getTempBuyerRequestId());
            dto.setOrganizationName(tempBuyer.getOrganizationName());
            dto.setContactEmail(tempBuyer.getContact() != null ? tempBuyer.getContact().getEmail() : null);
            dto.setCreatedAt(tempBuyer.getCreatedAt());
            dto.setStatus(tempBuyer.getStatus());
            dtos.add(dto);
        });
        return dtos;
    }

    @Override
    public TempBuyer findById(Long id) {
        return tempBuyerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("TempBuyer not found for id: " + id));
    }

    @Override
    public Optional<TempBuyer> findByUserId(Long buyerUserId) {
        return tempBuyerRepository.findByUser_BuyerUserId(buyerUserId);
    }

    @Override
    @Transactional
    public void updateGstVerification(Long tempBuyerId, boolean isGstVerified) {
        TempBuyer buyer = findById(tempBuyerId);
        buyer.setGstVerified(isGstVerified);
        tempBuyerRepository.save(buyer);
    }

    @Override
    @Transactional
    public void updatePanVerification(Long tempBuyerId, boolean isPanVerified) {
        TempBuyer buyer = findById(tempBuyerId);
        buyer.setPanVerified(isPanVerified);
        tempBuyerRepository.save(buyer);
    }

    @Override
    @Transactional
    public void updateDocumentVerification(Long tempBuyerId, Long documentId, boolean isDocumentVerified) {
        if (!tempBuyerRepository.existsById(tempBuyerId)) {
            throw new NotFoundException("TempBuyer not found for id: " + tempBuyerId);
        }
        TempBuyerDocument doc = tempBuyerDocumentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document not found for id: " + documentId));

        if (!doc.getBuyer().getTempBuyerId().equals(tempBuyerId)) {
            throw new ApplicationException("Document id=" + documentId + " does not belong to buyerId=" + tempBuyerId);
        }

        doc.setDocumentVerified(isDocumentVerified);
        tempBuyerDocumentRepository.save(doc);
    }

    @Override
    public void deleteTempBuyer(Long tempBuyerId) {
        TempBuyer tempBuyer = findById(tempBuyerId);
        deleteTempBuyerS3Files(tempBuyer);
        tempBuyerRepository.delete(tempBuyer);
    }

    @Override
    public void deleteBothBuyerAndTempBuyer(Long tempBuyerId) {
        TempBuyer tempBuyer = findById(tempBuyerId);

        if (TempBuyerStatus.APPROVED.equals(tempBuyer.getStatus()) && tempBuyer.getUser() != null) {
            buyerRepository.findByUser_BuyerUserId(tempBuyer.getUser().getBuyerUserId())
                    .ifPresent(buyer -> {
                        log.info("Deleting approved Buyer with buyerId: {}", buyer.getBuyerId());
                        buyerRepository.delete(buyer);
                    });
        }

        deleteTempBuyerS3Files(tempBuyer);
        tempBuyerRepository.delete(tempBuyer);
    }

    @Override
    @Transactional
    public TempBuyerResponseDTO updateTempBuyer(Long tempBuyerId, TempBuyerRequestDTO requestDTO) {
        TempBuyer buyer = findById(tempBuyerId);

        if (buyer.getStatus().equalsIgnoreCase(TempBuyerStatus.APPROVED)) {
            throw new ApplicationException("You are not allowed to update this application because it is already approved.");
        }
        if (buyer.getStatus().equalsIgnoreCase(TempBuyerStatus.REJECTED)) {
            throw new ApplicationException("You are not allowed to update this application because it is already rejected.");
        }

        BuyerTypeMaster buyerType = buyerTypeMasterRepository.findById(requestDTO.getBuyerTypeId())
                .orElseThrow(() -> new NotFoundException("Buyer type not found for id: " + requestDTO.getBuyerTypeId()));

        validatePanRequiredWhenNoGst(requestDTO.getGstNumber(), requestDTO.getPanNumber());

        buyer.setOrganizationName(requestDTO.getOrganizationName());
        buyer.setBuyerType(buyerType);
        buyer.setGstNumber(requestDTO.getGstNumber());
        buyer.setPanNumber(requestDTO.getPanNumber());
        buyer.setTermsAccepted(requestDTO.isTermsAccepted());
        buyer.setUpdatedBy("SYSTEM");

        applyAddressUpdate(requestDTO.getAddress(), buyer);
        applyContactUpdate(requestDTO.getContact(), buyer);

        TempBuyer saved = tempBuyerRepository.save(buyer);
        return mapToResponseDTO(saved);
    }

    private void applyAddressUpdate(TempBuyerAddressDTO dto, TempBuyer buyer) {
        if (dto == null) return;
        if (buyer.getAddress() == null) {
            buyer.setAddress(createAddress(dto, buyer));
            return;
        }
        TempBuyerAddress address = buyer.getAddress();
        stateMasterRepository.findById(dto.getStateId()).ifPresent(address::setState);
        districtMasterRepository.findById(dto.getDistrictId()).ifPresent(address::setDistrict);
        talukaMasterRepository.findById(dto.getTalukaId()).ifPresent(address::setTaluka);
        address.setCity(dto.getCity());
        address.setStreet(dto.getStreet());
        address.setBuildingNo(dto.getBuildingNo());
        address.setLandmark(dto.getLandmark());
        address.setPinCode(dto.getPinCode());
        address.setUpdatedBy("SYSTEM");
    }

    private void applyContactUpdate(TempBuyerContactDTO dto, TempBuyer buyer) {
        if (dto == null) return;
        if (buyer.getContact() == null) {
            buyer.setContact(createContact(dto, buyer));
            return;
        }
        TempBuyerContact contact = buyer.getContact();
        contact.setName(dto.getName());
        contact.setDesignation(dto.getDesignation());
        contact.setEmail(dto.getEmail());
        contact.setMobile(dto.getMobile());
        contact.setUpdatedBy("SYSTEM");
    }

    // ─── Draft flow ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public TempBuyerResponseDTO saveDraft(Long tempBuyerId, TempBuyerDraftRequestDTO dto) {
        TempBuyer buyer;

        if (tempBuyerId == null) {
            buyer = new TempBuyer();
            buyer.setTempBuyerRequestId(requestIdGeneratorService.generateNextRequestId());
            buyer.setCreatedBy("SYSTEM");
        } else {
            buyer = findById(tempBuyerId);
            if (!buyer.getStatus().equalsIgnoreCase(TempBuyerStatus.DRAFT)) {
                throw new ApplicationException(
                        "This registration is no longer a draft (status: " + buyer.getStatus()
                                + ") and can no longer be edited via the draft endpoint.");
            }
        }

        if (dto.getOrganizationName() != null) buyer.setOrganizationName(dto.getOrganizationName());
        if (dto.getOrgLogoUrl() != null) buyer.setOrgLogoUrl(dto.getOrgLogoUrl());
        if (dto.getGstNumber() != null) buyer.setGstNumber(dto.getGstNumber());
        if (dto.getPanNumber() != null) buyer.setPanNumber(dto.getPanNumber());
        buyer.setTermsAccepted(dto.isTermsAccepted());
        buyer.setStatus(TempBuyerStatus.DRAFT);
        buyer.setUpdatedBy("SYSTEM");

        if (dto.getBuyerUserId() != null) {
            buyerUserRepository.findById(dto.getBuyerUserId()).ifPresentOrElse(buyer::setUser,
                    () -> log.warn("saveDraft: buyerUserId {} not found — leaving user unset", dto.getBuyerUserId()));
        }

        BuyerTypeMaster buyerType = null;
        if (dto.getBuyerTypeId() != null) {
            Optional<BuyerTypeMaster> resolved = buyerTypeMasterRepository.findById(dto.getBuyerTypeId());
            if (resolved.isPresent()) {
                buyerType = resolved.get();
                buyer.setBuyerType(buyerType);
            } else {
                log.warn("saveDraft: buyerTypeId {} not found — leaving buyerType unset", dto.getBuyerTypeId());
            }
        } else {
            buyerType = buyer.getBuyerType();
        }

        if (draftAddressHasContent(dto.getAddress())) {
            TempBuyerAddress address = buyer.getAddress();
            if (address == null) {
                address = new TempBuyerAddress();
                address.setBuyer(buyer);
                address.setCreatedBy("SYSTEM");
                buyer.setAddress(address);
            }
            applyDraftAddress(dto.getAddress(), address);
        }

        if (draftContactHasContent(dto.getContact())) {
            TempBuyerContact contact = buyer.getContact();
            if (contact == null) {
                contact = new TempBuyerContact();
                contact.setBuyer(buyer);
                contact.setEmailVerified(false);
                contact.setPhoneVerified(false);
                contact.setCreatedBy("SYSTEM");
                buyer.setContact(contact);
            }
            applyDraftContact(dto.getContact(), contact);
        }

        // Ensure the mandatory document placeholder exists once a buyer type
        // is known, so its documentId is available for the upload endpoint.
        final BuyerTypeMaster resolvedBuyerType = buyerType;
        if (resolvedBuyerType != null && buyer.getDocuments().stream().noneMatch(
                d -> resolvedBuyerType.getMandatoryDocumentTypeId() != null
                        && d.getDocumentType() != null
                        && d.getDocumentType().getDocumentTypeId().equals(resolvedBuyerType.getMandatoryDocumentTypeId().getDocumentTypeId()))) {
            TempBuyerDocumentDto matching = findMatchingDocument(dto.getDocuments(), resolvedBuyerType);
            buyer.addDocument(createMandatoryDocument(resolvedBuyerType, matching));
        }

        TempBuyer saved = tempBuyerRepository.save(buyer);
        return mapToResponseDTO(saved);
    }

    @Override
    @Transactional
    public TempBuyerResponseDTO finalizeDraft(Long tempBuyerId, TempBuyerRequestDTO requestDTO) {
        TempBuyer buyer = findById(tempBuyerId);

        if (!buyer.getStatus().equalsIgnoreCase(TempBuyerStatus.DRAFT)) {
            throw new ApplicationException(
                    "Only a draft registration can be finalized (current status: " + buyer.getStatus() + ").");
        }

        BuyerTypeMaster buyerType = buyerTypeMasterRepository.findById(requestDTO.getBuyerTypeId())
                .orElseThrow(() -> new NotFoundException("Buyer type not found for id: " + requestDTO.getBuyerTypeId()));

        validatePanRequiredWhenNoGst(requestDTO.getGstNumber(), requestDTO.getPanNumber());

        buyer.setOrganizationName(requestDTO.getOrganizationName());
        buyer.setBuyerType(buyerType);
        buyer.setGstNumber(requestDTO.getGstNumber());
        buyer.setPanNumber(requestDTO.getPanNumber());
        buyer.setTermsAccepted(requestDTO.isTermsAccepted());
        buyer.setUpdatedBy("SYSTEM");

        applyAddressUpdate(requestDTO.getAddress(), buyer);
        applyContactUpdate(requestDTO.getContact(), buyer);

        if (buyer.getDocuments().isEmpty()) {
            TempBuyerDocumentDto matching = findMatchingDocument(requestDTO.getDocuments(), buyerType);
            buyer.addDocument(createMandatoryDocument(buyerType, matching));
        }

        buyer.setStatus(TempBuyerStatus.SUBMITTED);
        TempBuyer saved = tempBuyerRepository.save(buyer);
        return mapToResponseDTO(saved);
    }

    private boolean draftAddressHasContent(TempBuyerAddressDTO a) {
        return a != null && (a.getStateId() != null || a.getDistrictId() != null || a.getTalukaId() != null
                || !isBlank(a.getCity()) || !isBlank(a.getStreet()) || !isBlank(a.getBuildingNo())
                || !isBlank(a.getLandmark()) || !isBlank(a.getPinCode()));
    }

    private boolean draftContactHasContent(TempBuyerContactDTO c) {
        return c != null && (!isBlank(c.getName()) || !isBlank(c.getDesignation()) || !isBlank(c.getEmail())
                || !isBlank(c.getMobile()));
    }

    private void applyDraftAddress(TempBuyerAddressDTO dto, TempBuyerAddress address) {
        if (dto.getStateId() != null) {
            stateMasterRepository.findById(dto.getStateId()).ifPresentOrElse(address::setState,
                    () -> log.warn("saveDraft: stateId {} not found for address — leaving unset", dto.getStateId()));
        }
        if (dto.getDistrictId() != null) {
            districtMasterRepository.findById(dto.getDistrictId()).ifPresentOrElse(address::setDistrict,
                    () -> log.warn("saveDraft: districtId {} not found for address — leaving unset", dto.getDistrictId()));
        }
        if (dto.getTalukaId() != null) {
            talukaMasterRepository.findById(dto.getTalukaId()).ifPresentOrElse(address::setTaluka,
                    () -> log.warn("saveDraft: talukaId {} not found for address — leaving unset", dto.getTalukaId()));
        }
        if (dto.getCity() != null) address.setCity(dto.getCity());
        if (dto.getStreet() != null) address.setStreet(dto.getStreet());
        if (dto.getBuildingNo() != null) address.setBuildingNo(dto.getBuildingNo());
        if (dto.getLandmark() != null) address.setLandmark(dto.getLandmark());
        if (dto.getPinCode() != null) address.setPinCode(dto.getPinCode());
        address.setUpdatedBy("SYSTEM");
    }

    private void applyDraftContact(TempBuyerContactDTO dto, TempBuyerContact contact) {
        if (dto.getName() != null) contact.setName(dto.getName());
        if (dto.getDesignation() != null) contact.setDesignation(dto.getDesignation());
        if (dto.getEmail() != null) contact.setEmail(dto.getEmail());
        if (dto.getMobile() != null) contact.setMobile(dto.getMobile());
        contact.setUpdatedBy("SYSTEM");
    }

    // ─── Private helpers ────────────────────────────────────────────────────

    /**
     * PAN is required whenever GST is not supplied — enforced here (rather
     * than via a class-level bean-validation annotation) since it is a
     * simple either/or check across two top-level fields.
     */
    private void validatePanRequiredWhenNoGst(String gstNumber, String panNumber) {
        if (isBlank(gstNumber) && isBlank(panNumber)) {
            throw new ApplicationException("PAN number is required when GST number is not provided.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void deleteTempBuyerS3Files(TempBuyer tempBuyer) {
        deleteS3File(tempBuyer.getOrgLogoUrl());
        deleteS3File(tempBuyer.getGstFileUrl());
        deleteS3File(tempBuyer.getPanFileUrl());

        if (tempBuyer.getDocuments() != null) {
            tempBuyer.getDocuments().forEach(doc -> deleteS3File(doc.getDocumentFileUrl()));
        }
    }

    private void deleteS3File(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank() || PENDING.equalsIgnoreCase(fileUrl.trim())) return;
        try {
            String key = s3Service.extractKeyFromUrl(fileUrl);
            s3Service.deleteFile(key);
        } catch (Exception e) {
            log.warn("Could not delete S3 file for URL: {}. Reason: {}", fileUrl, e.getMessage());
        }
    }
}
