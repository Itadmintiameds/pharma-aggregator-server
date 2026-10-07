package com.example.pharmaaggregatorserver.dto.temp.buyer;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Response returned after successfully uploading TempBuyer documents to S3.
 * Mirrors dto.temp.seller.TempSellerDocumentUploadResponse.
 */
@Getter
@Setter
@Builder
public class TempBuyerDocumentUploadResponse {

    private Long tempBuyerId;
    private String tempBuyerRequestId;

    private String orgLogoUrl;
    private String orgLogoFileName;

    private String gstFileUrl;
    private String gstFileName;

    private String panFileUrl;
    private String panFileName;

    private List<LicenseUploadResult> licenseResults;

    @Getter
    @Setter
    @Builder
    public static class LicenseUploadResult {
        private Long documentId;
        private String documentFileUrl;
        private String documentFileName;
    }
}
