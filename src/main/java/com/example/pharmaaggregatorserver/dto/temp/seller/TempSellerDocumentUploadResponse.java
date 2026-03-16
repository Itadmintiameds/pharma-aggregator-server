package com.example.pharmaaggregatorserver.dto.temp.seller;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Response returned after successfully uploading TempSeller documents to S3.
 */
@Getter
@Setter
@Builder
public class TempSellerDocumentUploadResponse {

    private Long tempSellerId;
    private String tempSellerRequestId;

    /**
     * S3 URL of the uploaded seller profile image (null if not uploaded)
     */
    private String sellerImageUrl;

    /**
     * S3 URL of the uploaded GST certificate (null if not uploaded).
     */
    private String gstFileUrl;

    /**
     * S3 URL of the uploaded bank document (null if not uploaded).
     */
    private String bankDocumentFileUrl;

    /**
     * S3 URLs for each uploaded license document, in request order.
     */
    private List<LicenseUploadResult> licenseResults;

    @Getter
    @Setter
    @Builder
    public static class LicenseUploadResult {
        private Long documentId;
        private String licenseName;
        private String documentFileUrl;
    }
}