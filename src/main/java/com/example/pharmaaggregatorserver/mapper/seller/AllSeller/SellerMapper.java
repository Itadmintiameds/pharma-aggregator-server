package com.example.pharmaaggregatorserver.mapper.seller.AllSeller;

import com.example.pharmaaggregatorserver.dto.seller.SellerResponseDTO;
import com.example.pharmaaggregatorserver.entity.seller.*;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SellerMapper {

    public SellerResponseDTO toDTO(Seller seller) {
        if (seller == null) return null;

        return SellerResponseDTO.builder()
                .sellerId(seller.getSellerId())
                .sellerName(seller.getSellerName())
                .address(mapAddress(seller.getAddress()))
                .coordinator(mapCoordinator(seller.getCoordinator()))
                .bankDetails(mapBankDetails(seller.getBankDetails()))
                .sellerGST(mapGST(seller.getSellerGST()))
                .documents(mapDocuments(seller.getDocuments()))
                .productTypes(mapProductTypes(seller.getProductTypes()))
                .companyType(mapCompanyType(seller.getCompanyType()))
                .sellerType(mapSellerType(seller.getSellerType()))
                .phone(seller.getPhone())
                .isPhoneVerified(seller.isPhoneVerified())
                .email(seller.getEmail())
                .isEmailVerified(seller.isEmailVerified())
                .website(seller.getWebsite())
                .status(seller.getStatus())
                .termsAccepted(seller.isTermsAccepted())
                .user(mapUser(seller.getUser()))
                .createdAt(seller.getCreatedAt())
                .updatedAt(seller.getUpdatedAt())
                .isActive(seller.getIsActive())
                .build();
    }

    public List<SellerResponseDTO> toDTOList(List<Seller> sellers) {
        if (sellers == null) return Collections.emptyList();
        return sellers.stream().map(this::toDTO).collect(Collectors.toList());
    }

    private SellerResponseDTO.SellerAddressDTO mapAddress(SellerAddress address) {
        if (address == null) return null;
        return SellerResponseDTO.SellerAddressDTO.builder()
                .sellerAddressId(address.getSellerAddressId())
                .city(address.getCity())
                .street(address.getStreet())
                .buildingNo(address.getBuildingNo())
                .landmark(address.getLandmark())
                .pinCode(address.getPinCode())
                .state(address.getState() != null ? address.getState().getStateName() : null)
                .district(address.getDistrict() != null ? address.getDistrict().getDistrictName() : null)
                .taluka(address.getTaluka() != null ? address.getTaluka().getTalukaName() : null)
                .build();
    }

    private SellerResponseDTO.SellerCoordinatorDTO mapCoordinator(SellerCoordinator coordinator) {
        if (coordinator == null) return null;
        return SellerResponseDTO.SellerCoordinatorDTO.builder()
                .sellerCoordinatorId(coordinator.getSellerCoordinatorId())
                .name(coordinator.getName())
                .designation(coordinator.getDesignation())
                .email(coordinator.getEmail())
                .isEmailVerified(coordinator.isEmailVerified())
                .mobile(coordinator.getMobile())
                .isPhoneVerified(coordinator.isPhoneVerified())
                .build();
    }

    private SellerResponseDTO.SellerBankDetailsDTO mapBankDetails(SellerBankDetails bankDetails) {
        if (bankDetails == null) return null;
        return SellerResponseDTO.SellerBankDetailsDTO.builder()
                .sellerBankDetailsId(bankDetails.getSellerBankDetailsId())
                .bankName(bankDetails.getBankName())
                .branch(bankDetails.getBranch())
                .ifscCode(bankDetails.getIfscCode())
                .accountNumber(bankDetails.getAccountNumber())
                .accountHolderName(bankDetails.getAccountHolderName())
                .isBankDocumentVerified(bankDetails.isBankDocumentVerified())
                .build();
    }

    private SellerResponseDTO.SellerGSTDTO mapGST(SellerGST gst) {
        if (gst == null) return null;
        return SellerResponseDTO.SellerGSTDTO.builder()
                .sellerGstId(gst.getSellerGstId())
                .gstNumber(gst.getGstNumber())
                .isGstVerified(gst.isGstVerified())
                .build();
    }

    private List<SellerResponseDTO.SellerDocumentDTO> mapDocuments(List<SellerDocument> documents) {
        if (documents == null) return Collections.emptyList();
        return documents.stream().map(doc ->
                SellerResponseDTO.SellerDocumentDTO.builder()
                        .sellerDocumentsId(doc.getSellerDocumentsId())
                        .documentNumber(doc.getDocumentNumber())
                        .isDocumentVerified(doc.isDocumentVerified())
                        .productType(doc.getProductTypes() != null ? doc.getProductTypes().getProductTypeName() : null)
                        .build()
        ).collect(Collectors.toList());
    }

    private List<SellerResponseDTO.ProductTypeDTO> mapProductTypes(List<com.example.pharmaaggregatorserver.entity.master.ProductTypeMaster> productTypes) {
        if (productTypes == null) return Collections.emptyList();
        return productTypes.stream().map(pt ->
                SellerResponseDTO.ProductTypeDTO.builder()
                        .productTypeId(pt.getProductTypeId())
                        .productTypeName(pt.getProductTypeName())
                        .build()
        ).collect(Collectors.toList());
    }

    private SellerResponseDTO.CompanyTypeDTO mapCompanyType(com.example.pharmaaggregatorserver.entity.master.CompanyTypeMaster companyType) {
        if (companyType == null) return null;
        return SellerResponseDTO.CompanyTypeDTO.builder()
                .companyTypeId(companyType.getCompanyTypeId())
                .companyTypeName(companyType.getCompanyTypeName())
                .build();
    }

    private SellerResponseDTO.SellerTypeDTO mapSellerType(com.example.pharmaaggregatorserver.entity.master.SellerTypeMaster sellerType) {
        if (sellerType == null) return null;
        return SellerResponseDTO.SellerTypeDTO.builder()
                .sellerTypeId(sellerType.getSellerTypeId())
                .sellerTypeName(sellerType.getSellerTypeName())
                .build();
    }

    private SellerResponseDTO.UserDTO mapUser(com.example.pharmaaggregatorserver.entity.auth.User user) {
        if (user == null) return null;
        return SellerResponseDTO.UserDTO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .build();
    }
}
