package com.example.pharmaaggregatorserver.dto.temp.seller;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

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
     * Original filename of the uploaded GST certificate (null if not uploaded).
     */
    private String gstFileName;

    /**
     * S3 URL of the uploaded bank document (null if not uploaded).
     */
    private String bankDocumentFileUrl;

    /**
     * Original filename of the uploaded bank document (null if not uploaded).
     */
    private String bankDocumentFileName;

    /**
     * S3 URL of the uploaded Company Registration Certificate (null if not uploaded).
     */
    private String companyRegistrationCertificateUrl;

    /**
     * Original filename of the uploaded Company Registration Certificate (null if not uploaded).
     */
    private String companyRegistrationCertificateFileName;

    private String authorizationLetterUrl;

    /**
     * Original filename of the uploaded Authorization Letter (null if not uploaded).
     */
    private String authorizationLetterFileName;
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
        private String documentFileName;
    }
}