package com.example.pharmaaggregatorserver.service.temp.seller;

import com.example.pharmaaggregatorserver.dto.temp.seller.TempSellerDocumentUploadRequest;
import com.example.pharmaaggregatorserver.dto.temp.seller.TempSellerDocumentUploadResponse;

/**
 * Handles S3 file uploads for TempSeller registration documents.
 */
public interface TempSellerDocumentService {

    /**
     * Uploads GST, bank, and license files to S3 and persists the resulting URLs
     * on the corresponding DB entities.
     *
     * @param tempSellerId the PK of the already-created TempSeller record
     * @param request      multipart payload carrying the files
     * @return URLs of every uploaded file
     */
    TempSellerDocumentUploadResponse uploadDocuments(Long tempSellerId,
                                                     TempSellerDocumentUploadRequest request);
}