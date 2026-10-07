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

    /**
     * Deletes the seller's uploaded Company Registration Certificate file (if
     * a real one exists) from S3 and resets the field back to the "PENDING"
     * placeholder.
     */
    void deleteCompanyRegistrationCertificate(Long tempSellerId);

    /**
     * Deletes the seller's uploaded GST file (if a real one exists) from S3
     * and clears the field.
     */
    void deleteGstFile(Long tempSellerId);

    /**
     * Deletes the coordinator's uploaded authorization letter (if a real one
     * exists) from S3 and resets the field back to the "PENDING" placeholder.
     */
    void deleteAuthorizationLetter(Long tempSellerId);

    /**
     * Deletes the seller's uploaded bank document (if a real one exists) from
     * S3 and clears the field.
     */
    void deleteBankDocument(Long tempSellerId);

    /**
     * Deletes the file attached to a single {@code TempSellerDocument} row
     * (if a real one exists) from S3 and resets it back to the "PENDING"
     * placeholder, leaving the row itself (and its documentId) intact so it
     * can be re-uploaded to later.
     */
    void deleteDocumentFile(Long tempSellerId, Long documentId);
}