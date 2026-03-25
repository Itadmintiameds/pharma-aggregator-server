package com.example.pharmaaggregatorserver.mapper.seller.profile;

import com.example.pharmaaggregatorserver.dto.seller.profile.SellerResponseDTO;
import com.example.pharmaaggregatorserver.entity.master.ProductTypeMaster;
import com.example.pharmaaggregatorserver.entity.seller.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SellerProfileMapper {

    /**
     * Map Seller entity to SellerResponseDTO
     */
    public SellerResponseDTO toSellerResponseDTO(Seller seller) {
        if (seller == null) {
            return null;
        }

        SellerResponseDTO dto = new SellerResponseDTO();

        // Basic info
        dto.setSellerId(seller.getSellerId());
        dto.setSellerName(seller.getSellerName());
        dto.setPhone(seller.getPhone());
        dto.setEmail(seller.getEmail());
        dto.setWebsite(seller.getWebsite());
        dto.setTermsAccepted(seller.isTermsAccepted());

        // GST info
        if (seller.getSellerGST() != null) {
            dto.setGstNumber(seller.getSellerGST().getGstNumber());
            dto.setGstFileUrl(seller.getSellerGST().getGstFileUrl());
        }

        // Company Type
        if (seller.getCompanyType() != null) {
            dto.setCompanyTypeId(seller.getCompanyType().getCompanyTypeId());
        }

        // Seller Type
        if (seller.getSellerType() != null) {
            dto.setSellerTypeId(seller.getSellerType().getSellerTypeId());
        }

        // Address
        if (seller.getAddress() != null) {
            dto.setAddress(mapToAddressDTO(seller.getAddress()));
        }

        // Coordinator
        if (seller.getCoordinator() != null) {
            dto.setCoordinator(mapToCoordinatorDTO(seller.getCoordinator()));
        }

        // Bank Details
        if (seller.getBankDetails() != null) {
            dto.setBankDetails(mapToBankDetailsDTO(seller.getBankDetails()));
        }

        // Product Types - set only IDs
        if (seller.getProductTypes() != null && !seller.getProductTypes().isEmpty()) {
            List<Long> productTypeIds = seller.getProductTypes().stream()
                    .map(ProductTypeMaster::getProductTypeId)
                    .collect(Collectors.toList());
            dto.setProductTypeId(productTypeIds);
        }

        // Documents
        if (seller.getDocuments() != null && !seller.getDocuments().isEmpty()) {
            List<SellerResponseDTO.DocumentDTO> documentDTOs = seller.getDocuments().stream()
                    .map(this::mapToDocumentDTO)
                    .collect(Collectors.toList());
            dto.setDocuments(documentDTOs);
        }

        return dto;
    }

    /**
     * Map SellerAddress to AddressDTO
     */
    private SellerResponseDTO.AddressDTO mapToAddressDTO(SellerAddress address) {
        SellerResponseDTO.AddressDTO addressDTO = new SellerResponseDTO.AddressDTO();

        if (address.getState() != null) {
            addressDTO.setStateId(address.getState().getStateId());
        }

        if (address.getDistrict() != null) {
            addressDTO.setDistrictId(address.getDistrict().getDistrictId());
        }

        if (address.getTaluka() != null) {
            addressDTO.setTalukaId(address.getTaluka().getTalukaId());
        }

        addressDTO.setCity(address.getCity());
        addressDTO.setStreet(address.getStreet());
        addressDTO.setBuildingNo(address.getBuildingNo());
        addressDTO.setLandmark(address.getLandmark());
        addressDTO.setPinCode(address.getPinCode());

        return addressDTO;
    }

    /**
     * Map SellerCoordinator to CoordinatorDTO
     */
    private SellerResponseDTO.CoordinatorDTO mapToCoordinatorDTO(SellerCoordinator coordinator) {
        SellerResponseDTO.CoordinatorDTO coordinatorDTO = new SellerResponseDTO.CoordinatorDTO();
        coordinatorDTO.setName(coordinator.getName());
        coordinatorDTO.setDesignation(coordinator.getDesignation());
        coordinatorDTO.setEmail(coordinator.getEmail());
        coordinatorDTO.setMobile(coordinator.getMobile());
        return coordinatorDTO;
    }

    /**
     * Map SellerBankDetails to BankDetailsDTO
     */
    private SellerResponseDTO.BankDetailsDTO mapToBankDetailsDTO(SellerBankDetails bankDetails) {
        SellerResponseDTO.BankDetailsDTO bankDTO = new SellerResponseDTO.BankDetailsDTO();
        bankDTO.setBankName(bankDetails.getBankName());
        bankDTO.setBranch(bankDetails.getBranch());
        bankDTO.setIfscCode(bankDetails.getIfscCode());
        bankDTO.setAccountNumber(bankDetails.getAccountNumber());
        bankDTO.setAccountHolderName(bankDetails.getAccountHolderName());
        bankDTO.setBankDocumentFileUrl(bankDetails.getBankDocumentFileUrl());
        return bankDTO;
    }

    /**
     * Map SellerDocument to DocumentDTO
     */
    private SellerResponseDTO.DocumentDTO mapToDocumentDTO(SellerDocument document) {
        SellerResponseDTO.DocumentDTO dto = new SellerResponseDTO.DocumentDTO();

        if (document.getProductTypes() != null) {
            dto.setProductTypeId(document.getProductTypes().getProductTypeId());
        }

        dto.setDocumentNumber(document.getDocumentNumber());
        dto.setDocumentFileUrl(document.getDocumentFileUrl());
        dto.setLicenseIssueDate(document.getLicenseIssueDate());
        dto.setLicenseExpiryDate(document.getLicenseExpiryDate());
        dto.setLicenseIssuingAuthority(document.getLicenseIssuingAuthority());

        return dto;
    }
}