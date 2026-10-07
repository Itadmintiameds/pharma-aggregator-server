package com.example.pharmaaggregatorserver.mapper.seller;

import com.example.pharmaaggregatorserver.dto.seller.SellerBankDetailsDTO;
import com.example.pharmaaggregatorserver.entity.seller.SellerBankDetails;

public class SellerBankDetailsMapper {

    public static SellerBankDetailsDTO toDto(SellerBankDetails sellerBankDetails) {
        if (sellerBankDetails == null) return null;
        return SellerBankDetailsDTO
                .builder()
                .sellerBankDetailsId(sellerBankDetails.getSellerBankDetailsId())
                .bankName(sellerBankDetails.getBankName())
                .branch(sellerBankDetails.getBranch())
                .ifscCode(sellerBankDetails.getIfscCode())
                .accountNumber(sellerBankDetails.getAccountNumber())
                .accountHolderName(sellerBankDetails.getAccountHolderName())
                .bankDocumentFileUrl(sellerBankDetails.getBankDocumentFileUrl())
                .build();
    }

    public static SellerBankDetails toEntity(SellerBankDetailsDTO sellerBankDetailsDTO) {
        if (sellerBankDetailsDTO == null) return null;
        return SellerBankDetails
                .builder()
                .sellerBankDetailsId(sellerBankDetailsDTO.getSellerBankDetailsId())
                .bankName(sellerBankDetailsDTO.getBankName())
                .branch(sellerBankDetailsDTO.getBranch())
                .ifscCode(sellerBankDetailsDTO.getIfscCode())
                .accountNumber(sellerBankDetailsDTO.getAccountNumber())
                .accountHolderName(sellerBankDetailsDTO.getAccountHolderName())
                .bankDocumentFileUrl(sellerBankDetailsDTO.getBankDocumentFileUrl())
                .build();
    }
}
