package com.example.pharmaaggregatorserver.mapper.seller.profile;

import com.example.pharmaaggregatorserver.dto.seller.profile.SellerResponseDTO;
import com.example.pharmaaggregatorserver.entity.seller.*;
import com.example.pharmaaggregatorserver.entity.master.ProductTypeMaster;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SellerByIdMapper {

    public SellerResponseDTO toResponseDTO(Seller seller) {
        if (seller == null) {
            return null;
        }

        SellerResponseDTO dto = new SellerResponseDTO();

        // Basic info - Convert String sellerId to Long
        if (seller.getSellerId() != null) {
            try {
                dto.setSellerId(Long.parseLong(seller.getSellerId()));
            } catch (NumberFormatException e) {
                dto.setSellerId(null);
            }
        }
        dto.setSellerName(seller.getSellerName());
        dto.setPhone(seller.getPhone());
        dto.setEmail(seller.getEmail());
        dto.setWebsite(seller.getWebsite());
        dto.setTermsAccepted(seller.isTermsAccepted());
        dto.setCompanyRegistrationCertificateUrl(seller.getCompanyRegistrationCertificateUrl());
        dto.setCreatedAt(seller.getCreatedAt());
        dto.setUpdatedAt(seller.getUpdatedAt());

        // Company Type
        if (seller.getCompanyType() != null) {
            dto.setCompanyTypeId(seller.getCompanyType().getCompanyTypeId());
            dto.setCompanyTypeName(seller.getCompanyType().getCompanyTypeName());
        }

        // Seller Type
        if (seller.getSellerType() != null) {
            dto.setSellerTypeId(seller.getSellerType().getSellerTypeId());
            dto.setSellerTypeName(seller.getSellerType().getSellerTypeName());
        }

        // GST
        if (seller.getSellerGST() != null) {
            dto.setGstNumber(seller.getSellerGST().getGstNumber());
            dto.setGstFileUrl(seller.getSellerGST().getGstFileUrl());
        }

        // Product Types
        if (seller.getProductTypes() != null && !seller.getProductTypes().isEmpty()) {
            // Set product type IDs
            List<Long> productTypeIds = seller.getProductTypes().stream()
                    .map(ProductTypeMaster::getProductTypeId)
                    .collect(Collectors.toList());
            dto.setProductTypeId(productTypeIds);

            // Set product types with names
            List<SellerResponseDTO.ProductTypeDTO> productTypeDTOs = seller.getProductTypes().stream()
                    .map(pt -> {
                        SellerResponseDTO.ProductTypeDTO ptDTO = new SellerResponseDTO.ProductTypeDTO();
                        ptDTO.setProductTypeId(pt.getProductTypeId());
                        ptDTO.setProductTypeName(pt.getProductTypeName());
                        return ptDTO;
                    })
                    .collect(Collectors.toList());
            dto.setProductTypes(productTypeDTOs);
        }

        // Address
        if (seller.getAddress() != null) {
            dto.setAddress(mapAddressToDTO(seller.getAddress()));
        }

        // Coordinator
        if (seller.getCoordinator() != null) {
            dto.setCoordinator(mapCoordinatorToDTO(seller.getCoordinator()));
        }

        // Bank Details
        if (seller.getBankDetails() != null) {
            dto.setBankDetails(mapBankDetailsToDTO(seller.getBankDetails()));
        }

        // Documents
        if (seller.getDocuments() != null && !seller.getDocuments().isEmpty()) {
            dto.setDocuments(mapDocumentsToDTO(seller.getDocuments()));
        }

        return dto;
    }

    private SellerResponseDTO.AddressDTO mapAddressToDTO(SellerAddress address) {
        SellerResponseDTO.AddressDTO addressDTO = new SellerResponseDTO.AddressDTO();

        if (address.getState() != null) {
            addressDTO.setStateId(address.getState().getStateId());
            addressDTO.setStateName(address.getState().getStateName());
        }
        if (address.getDistrict() != null) {
            addressDTO.setDistrictId(address.getDistrict().getDistrictId());
            addressDTO.setDistrictName(address.getDistrict().getDistrictName());
        }
        if (address.getTaluka() != null) {
            addressDTO.setTalukaId(address.getTaluka().getTalukaId());
            addressDTO.setTalukaName(address.getTaluka().getTalukaName());
        }
        addressDTO.setCity(address.getCity());
        addressDTO.setStreet(address.getStreet());
        addressDTO.setBuildingNo(address.getBuildingNo());
        addressDTO.setLandmark(address.getLandmark());
        addressDTO.setPinCode(address.getPinCode());

        return addressDTO;
    }

    private SellerResponseDTO.CoordinatorDTO mapCoordinatorToDTO(SellerCoordinator coordinator) {
        SellerResponseDTO.CoordinatorDTO coordinatorDTO = new SellerResponseDTO.CoordinatorDTO();
        coordinatorDTO.setName(coordinator.getName());
        coordinatorDTO.setDesignation(coordinator.getDesignation());
        coordinatorDTO.setEmail(coordinator.getEmail());
        coordinatorDTO.setMobile(coordinator.getMobile());
        return coordinatorDTO;
    }

    private SellerResponseDTO.BankDetailsDTO mapBankDetailsToDTO(SellerBankDetails bank) {
        SellerResponseDTO.BankDetailsDTO bankDTO = new SellerResponseDTO.BankDetailsDTO();
        bankDTO.setBankName(bank.getBankName());
        bankDTO.setBranch(bank.getBranch());
        bankDTO.setIfscCode(bank.getIfscCode());
        bankDTO.setAccountNumber(bank.getAccountNumber());
        bankDTO.setAccountHolderName(bank.getAccountHolderName());
        bankDTO.setBankDocumentFileUrl(bank.getBankDocumentFileUrl());
        return bankDTO;
    }

    private List<SellerResponseDTO.DocumentDTO> mapDocumentsToDTO(List<SellerDocument> documents) {
        return documents.stream()
                .map(doc -> {
                    SellerResponseDTO.DocumentDTO docDTO = new SellerResponseDTO.DocumentDTO();
                    if (doc.getProductTypes() != null) {
                        docDTO.setProductTypeId(doc.getProductTypes().getProductTypeId());
                        docDTO.setProductTypeName(doc.getProductTypes().getProductTypeName());
                    }
                    docDTO.setDocumentNumber(doc.getDocumentNumber());
                    docDTO.setDocumentFileUrl(doc.getDocumentFileUrl());

                    // Direct assignment since both are LocalDate
                    docDTO.setLicenseIssueDate(doc.getLicenseIssueDate());
                    docDTO.setLicenseExpiryDate(doc.getLicenseExpiryDate());

                    docDTO.setLicenseIssuingAuthority(doc.getLicenseIssuingAuthority());
                    return docDTO;
                })
                .collect(Collectors.toList());
    }
}