package com.example.pharmaaggregatorserver.service.serviceImpl.temp.seller;

import com.example.pharmaaggregatorserver.dto.admin.TempSellerAdminResponseDTO;
import com.example.pharmaaggregatorserver.dto.seller.*;
import com.example.pharmaaggregatorserver.entity.master.*;
import com.example.pharmaaggregatorserver.entity.temp.seller.*;
import com.example.pharmaaggregatorserver.exception.NotFoundException;
import com.example.pharmaaggregatorserver.repository.master.*;
import com.example.pharmaaggregatorserver.repository.temp.seller.TempSellerRepository;
import com.example.pharmaaggregatorserver.service.temp.seller.RequestIdGeneratorService;
import com.example.pharmaaggregatorserver.service.temp.seller.TempSellerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TempSellerServiceImpl implements TempSellerService {

    private final TempSellerRepository tempSellerRepository;
    private final ProductTypeMasterRepository productTypeMasterRepository;
    private final CompanyTypeMasterRepository companyTypeMasterRepository;
    private final SellerTypeMasterRepository sellerTypeMasterRepository;
    private final StateMasterRepository stateMasterRepository;
    private final DistrictMasterRepository districtMasterRepository;
    private final TalukaMasterRepository talukaMasterRepository;
    private final RequestIdGeneratorService requestIdGeneratorService;

    @Override
    @Transactional
    public TempSellerResponseDTO createTempSeller(TempSellerRequestDTO requestDTO) {
        String generatedRequestId = requestIdGeneratorService.generateNextRequestId();
        // Check if phone or email already exists
        if (tempSellerRepository.existsByPhone(requestDTO.getPhone())) {
            throw new RuntimeException("Phone number already exists");
        }
        if (tempSellerRepository.existsByEmail(requestDTO.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

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
        seller.setStatus("open"); // Initial status
        seller.setPhoneVerified(false);
        seller.setEmailVerified(false);
        seller.setCreatedBy("SYSTEM"); // You can get from SecurityContext
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

        // Prepare and return response
        return mapToResponseDTO(savedSeller);
    }

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

    private TempSellerCoordinator createCoordinator(TempSellerCoordinatorDTO coordinatorDTO, TempSeller seller) {
        TempSellerCoordinator coordinator = new TempSellerCoordinator();
        coordinator.setSeller(seller);
        coordinator.setName(coordinatorDTO.getName());
        coordinator.setDesignation(coordinatorDTO.getDesignation());
        coordinator.setEmail(coordinatorDTO.getEmail());
        coordinator.setMobile(coordinatorDTO.getMobile());
        // Set verification status for coordinator
        coordinator.setEmailVerified(false);  // Default to false
        coordinator.setPhoneVerified(false);  // Default to false
        coordinator.setCreatedBy("SYSTEM");
        coordinator.setUpdatedBy("SYSTEM");

        return coordinator;
    }

    private TempSellerBankDetails createBankDetails(TempSellerBankDetailsDTO bankDetailsDTO, TempSeller seller) {
        TempSellerBankDetails bankDetails = new TempSellerBankDetails();
        bankDetails.setSeller(seller);
        bankDetails.setBankName(bankDetailsDTO.getBankName());
        bankDetails.setBranch(bankDetailsDTO.getBranch());
        bankDetails.setIfscCode(bankDetailsDTO.getIfscCode());
        bankDetails.setAccountNumber(bankDetailsDTO.getAccountNumber());
        bankDetails.setAccountHolderName(bankDetailsDTO.getAccountHolderName());
        bankDetails.setCreatedBy("SYSTEM");
        bankDetails.setUpdatedBy("SYSTEM");

        return bankDetails;
    }

    private TempSellerDocument createDocument(TempSellerDocumentDTO docDTO, TempSeller seller) {
        ProductTypeMaster productType = productTypeMasterRepository.findById(docDTO.getProductTypeId())
                .orElseThrow(() -> new RuntimeException("Product type not found for document"));

        TempSellerDocument document = new TempSellerDocument();
        document.setSeller(seller);
        document.setGSTNumber(docDTO.getGstNumber());
        document.setGSTFileUrl(docDTO.getGstFileUrl());
        document.setProductTypes(productType);
        document.setDocumentNumber(docDTO.getDocumentNumber());
        document.setDocumentFileUrl(docDTO.getDocumentFileUrl());
        document.setCreatedBy("SYSTEM");
        document.setUpdatedBy("SYSTEM");

        return document;
    }

    private TempSellerResponseDTO mapToResponseDTO(TempSeller seller) {
        TempSellerResponseDTO responseDTO = new TempSellerResponseDTO();
        responseDTO.setTempSellerId(seller.getTempSellerId());
        responseDTO.setSellerName(seller.getSellerName());
        responseDTO.setSellerRequestId(seller.getTempSellerRequestId());
        responseDTO.setPhone(seller.getPhone());
        responseDTO.setEmail(seller.getEmail());
        responseDTO.setStatus(seller.getStatus());
        responseDTO.setCreatedAt(seller.getCreatedAt());
        // Map other fields as needed

        return responseDTO;
    }

    /* Get All Temporary Sellers */
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

    /* Get Temporary Seller By Id */
    @Override
    public TempSeller findById(Long id) {
        return tempSellerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("TempSeller not found for id: " + id));
    }
}
