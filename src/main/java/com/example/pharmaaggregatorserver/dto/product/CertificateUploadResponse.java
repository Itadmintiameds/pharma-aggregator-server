package com.example.pharmaaggregatorserver.dto.product;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CertificateUploadResponse {
    private String productAttributeId;
    private String attributeType;                                   // "consumable" | "non-consumable"
    private List<CertificateDocumentDto> uploadedCertificates;      // null when only brochure uploaded
    private String brochureUrl;                                     // null when only certificates uploaded
}
