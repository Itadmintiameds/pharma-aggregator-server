package com.example.pharmaaggregatorserver.mapper.seller;

import com.example.pharmaaggregatorserver.dto.seller.SellerDTO;
import com.example.pharmaaggregatorserver.entity.master.ProductTypeMaster;
import com.example.pharmaaggregatorserver.entity.seller.Seller;
import com.example.pharmaaggregatorserver.mapper.master.ProductTypeMapper;

import java.util.stream.Collectors;

public class SellerMapper {

    public static SellerDTO toDto(Seller seller) {
        if (seller == null) return null;
        return SellerDTO
                .builder()
                .sellerId(seller.getSellerId())
                .sellerName(seller.getSellerName())
                .sellerImageUrl(seller.getSellerImageUrl())
                .address(SellerAddressMapper.toDto(seller.getAddress()))
                .coordinator(SellerCoordinatorMapper.toDto(seller.getCoordinator()))
                .bankDetails(SellerBankDetailsMapper.toDto(seller.getBankDetails()))
                .sellerGST(SellerGSTMapper.toDto(seller.getSellerGST()))
                .documents(seller.getDocuments().stream()
                        .map(SellerDocumentMapper::toDto) // Map to DTOs
                        .collect(Collectors.toList()))
                .productTypeIds(seller.getProductTypes().stream()
                        .map(ProductTypeMaster::getProductTypeId)  // Extract IDs
                        .collect(Collectors.toList()))
                .productTypes(seller.getProductTypes().stream()
                        .map(ProductTypeMapper::toDto)  // Map to DTOs
                        .collect(Collectors.toList()))
                .companyTypeId(seller.getCompanyType() != null ? seller.getCompanyType().getCompanyTypeId() : null)
                .companyTypeName(seller.getCompanyType() != null ? seller.getCompanyType().getCompanyTypeName() : null)
                .sellerTypeId(seller.getSellerType() != null ? seller.getSellerType().getSellerTypeId() : null)
                .sellerTypeName(seller.getSellerType() != null ? seller.getSellerType().getSellerTypeName() : null)
                .phone(seller.getPhone())
                .isPhoneVerified(seller.isPhoneVerified())
                .email(seller.getEmail())
                .isEmailVerified(seller.isEmailVerified())
                .website(seller.getWebsite())
                .status(seller.getStatus())
                .termsAccepted(seller.isTermsAccepted())
                .isPasswordTemporary(seller.getUser().isPasswordTemporary())
                .build();
    }
}
