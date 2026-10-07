package com.example.pharmaaggregatorserver.mapper.seller;

import com.example.pharmaaggregatorserver.dto.seller.SellerGSTDTO;
import com.example.pharmaaggregatorserver.entity.seller.SellerGST;

public class SellerGSTMapper {

    public static SellerGSTDTO toDto(SellerGST sellerGST) {
        if (sellerGST == null) return null;

        return SellerGSTDTO
                .builder()
                .sellerGstId(sellerGST.getSellerGstId())
                .gstNumber(sellerGST.getGstNumber())
                .gstFileUrl(sellerGST.getGstFileUrl())
                .isGstVerified(sellerGST.isGstVerified())
                .build();
    }

    public static SellerGST toEntity(SellerGSTDTO sellerGSTDTO) {
        if (sellerGSTDTO == null) return null;
        return SellerGST
                .builder()
                .sellerGstId(sellerGSTDTO.getSellerGstId())
                .gstNumber(sellerGSTDTO.getGstNumber())
                .gstFileUrl(sellerGSTDTO.getGstFileUrl())
                .isGstVerified(sellerGSTDTO.isGstVerified())
                .build();
    }
}
