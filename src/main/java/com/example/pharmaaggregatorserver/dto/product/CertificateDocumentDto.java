package com.example.pharmaaggregatorserver.dto.product;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CertificateDocumentDto {
    private Long productCertificateDocumentId;
    private Long certificationId;
    private String certificationName;
    private String certificateUrl;
}
