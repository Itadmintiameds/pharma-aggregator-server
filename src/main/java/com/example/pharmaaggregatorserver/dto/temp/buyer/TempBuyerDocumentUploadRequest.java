package com.example.pharmaaggregatorserver.dto.temp.buyer;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Multipart request DTO for uploading TempBuyer files (org logo, GST, PAN,
 * license documents). Mirrors dto.temp.seller.TempSellerDocumentUploadRequest.
 */
@Getter
@Setter
public class TempBuyerDocumentUploadRequest {

    /**
     * Organization logo. Stored at:
     * tempbuyers/{REQ_ID}/orglogo/ORG_LOGO_{timestamp}.{ext}
     */
    private MultipartFile orgLogo;

    /**
     * GST certificate file. Stored at:
     * tempbuyers/{REQ_ID}/gst/GST_IMAGE_{timestamp}.{ext}
     */
    private MultipartFile gstFile;

    /**
     * PAN card file. Stored at:
     * tempbuyers/{REQ_ID}/pan/PAN_IMAGE_{timestamp}.{ext}
     */
    private MultipartFile panFile;

    /**
     * License/document files — one per existing TempBuyerDocument record.
     * Must match the order (and count) of {@code licenseNames}/{@code documentIds}.
     * Stored at: tempbuyers/{REQ_ID}/licenses/{DOC_TYPE_NAME}_{timestamp}.{ext}
     */
    private List<MultipartFile> licenseFiles;

    private List<String> licenseNames;

    private List<Long> documentIds;
}
