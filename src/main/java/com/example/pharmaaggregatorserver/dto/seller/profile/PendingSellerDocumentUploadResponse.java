package com.example.pharmaaggregatorserver.dto.seller.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PendingSellerDocumentUploadResponse {

    private Long pendingSellerId;

    /** Uploaded GST certificate URL (null if no file was sent) */
    private String gstFileUrl;

    /** Uploaded bank document URL (null if no file was sent) */
    private String bankDocumentFileUrl;

    /** Uploaded Company Registration certificate URL (null if no file was sent) */
    private String companyRegistrationCertificateUrl;

    /** One entry per license file that was uploaded */
    private List<LicenseUploadResult> licenseResults;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LicenseUploadResult {
        private Long documentId;
        private String licenseName;
        private String documentFileUrl;
    }
}