package com.example.pharmaaggregatorserver.service.temp.buyer;

import com.example.pharmaaggregatorserver.dto.temp.buyer.TempBuyerDocumentUploadRequest;
import com.example.pharmaaggregatorserver.dto.temp.buyer.TempBuyerDocumentUploadResponse;

/**
 * Handles S3 file uploads for TempBuyer registration documents. Mirrors
 * service.temp.seller.TempSellerDocumentService.
 */
public interface TempBuyerDocumentService {

    TempBuyerDocumentUploadResponse uploadDocuments(Long tempBuyerId,
                                                     TempBuyerDocumentUploadRequest request);

    void deleteGstFile(Long tempBuyerId);

    void deletePanFile(Long tempBuyerId);

    void deleteOrgLogo(Long tempBuyerId);

    /**
     * Deletes the file attached to a single {@code TempBuyerDocument} row
     * (if a real one exists) from S3 and resets it back to the "PENDING"
     * placeholder, leaving the row itself (and its documentId) intact.
     */
    void deleteDocumentFile(Long tempBuyerId, Long documentId);
}
