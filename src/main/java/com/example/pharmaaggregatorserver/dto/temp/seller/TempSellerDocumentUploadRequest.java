package com.example.pharmaaggregatorserver.dto.temp.seller;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Multipart request DTO for uploading TempSeller files (GST, Bank, License documents).
 * <p>
 * Usage (multipart/form-data):
 * gstFile          → single GST certificate image/pdf
 * bankFile         → single bank document image/pdf
 * licenseFiles     → list of license files (one per document record, in order)
 * licenseNames     → list of license names matching licenseFiles order (e.g. "Drug", "Wholesale")
 */
@Getter
@Setter
public class TempSellerDocumentUploadRequest {

    /**
     * Seller profile image / logo
     * Stored at: tempsellers/{REQ_ID}/sellerimage/SELLER_IMAGE_{timestamp}.{ext}
     */
    private MultipartFile sellerImage;

    /**
     * GST certificate file.
     * Stored at: tempsellers/{REQ_ID}/gst/GST_IMAGE_{timestamp}.{ext}
     */
    private MultipartFile gstFile;

    /**
     * Bank document file (cancelled cheque / passbook).
     * Stored at: tempsellers/{REQ_ID}/bankdocument/BANK_DETAILS_{timestamp}.{ext}
     */
    private MultipartFile bankFile;

    /**
     * License/document files — one per existing TempSellerDocument record.
     * These must match the order (and count) of {@code licenseNames}.
     * Stored at: tempsellers/{REQ_ID}/licenses/{LICENSE_NAME}_{timestamp}.{ext}
     */
    private List<MultipartFile> licenseFiles;

    /**
     * Names for each license file (e.g. "Drug", "Wholesale").
     * Used to build the S3 key: {LICENSE_NAME}_{timestamp}.{ext}
     */
    private List<String> licenseNames;

    /**
     * DB IDs of the TempSellerDocument rows to update, in the same order as
     * {@code licenseFiles} / {@code licenseNames}.
     */
    private List<Long> documentIds;
}