package com.example.pharmaaggregatorserver.mapper.seller;

import com.example.pharmaaggregatorserver.dto.seller.SellerDocumentDto;
import com.example.pharmaaggregatorserver.entity.seller.SellerDocument;

public class SellerDocumentMapper {

    public static SellerDocumentDto toDto(SellerDocument sellerDocument) {
        if (sellerDocument == null) return null;
        return SellerDocumentDto
                .builder()
                .sellerDocumentsId(sellerDocument.getSellerDocumentsId())
                .productTypeId(sellerDocument.getProductTypes().getProductTypeId())
                .documentNumber(sellerDocument.getDocumentNumber())
                .documentFileUrl(sellerDocument.getDocumentFileUrl())
                .isDocumentVerified(sellerDocument.isDocumentVerified())
                .build();
    }

    public static SellerDocument toEntity(SellerDocumentDto sellerDocumentDto) {
        if (sellerDocumentDto == null) return null;
        return SellerDocument
                .builder()
                .sellerDocumentsId(sellerDocumentDto.getSellerDocumentsId())
                .documentNumber(sellerDocumentDto.getDocumentNumber())
                .documentFileUrl(sellerDocumentDto.getDocumentFileUrl())
                .isDocumentVerified(sellerDocumentDto.isDocumentVerified())
                .build();
    }
}
