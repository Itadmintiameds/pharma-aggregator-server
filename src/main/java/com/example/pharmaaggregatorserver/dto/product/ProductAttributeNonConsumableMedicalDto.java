package com.example.pharmaaggregatorserver.dto.product;

import lombok.Data;

import java.util.List;

@Data
public class ProductAttributeNonConsumableMedicalDto {

    private String productAttributeId;
    private Long deviceCategoryId;
    private String deviceName;
    private String brandName;
    private String modelName;
    private String modelNumber;
    private String deviceClassification;
    private String udiNumber;
    private String purpose;
    private String keyFeaturesSpecifications;
//    private Long certificationId;
//    private String certificationName;
//    private String complianceCertificateUrl;

    // Replaces single certificationId + certificationName + complianceCertificateUrl
    private List<ProductCertificateDocumentDto> certificateDocuments;

    private Long materialTypeId;
    private String materialTypeName;
    private String warrantyPeriod;
    private boolean serviceAvailability;
    private String countryOfOrigin;
    private String manufacturerName;
    private String storageCondition;
    private String brochurePath;
}
