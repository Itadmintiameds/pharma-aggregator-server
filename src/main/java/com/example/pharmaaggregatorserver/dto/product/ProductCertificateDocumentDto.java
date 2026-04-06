package com.example.pharmaaggregatorserver.dto.product;

import lombok.Data;

@Data
public class ProductCertificateDocumentDto {
    private Long productCertificateDocumentId;      // ProductCertificateDocument Primary Key
    private Long certificationId;          // e.g. ID of CDSCO / ISO / CE / BIS
    private String certificationName;      // display name
    private String certificateUrl;         // uploaded URL or file path
}
