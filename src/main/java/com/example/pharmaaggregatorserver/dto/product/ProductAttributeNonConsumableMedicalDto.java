package com.example.pharmaaggregatorserver.dto.product;

import lombok.Data;

import java.util.List;

@Data
public class ProductAttributeNonConsumableMedicalDto {

    private String productAttributeId;
    private Long deviceCategoryId;
    private String deviceName;
    private Long deviceSubCategoryId;
    private String deviceSubCategoryName;
    private String brandName;
    private String modelName;
    private String modelNumber;
    private String deviceClassification;
    private String udiNumber;
    private String purpose;
    private String dimensionSize;
    private Long deviceSpecificationUnitId;
    private String deviceSpecificationUnitName;
    private String keyFeaturesSpecifications;
//    private Long certificationId;
//    private String certificationName;
//    private String complianceCertificateUrl;

    // Replaces single certificationId + certificationName + complianceCertificateUrl
    private List<ProductCertificateDocumentDto> certificateDocuments;

    private List<Long> materialTypeIds;
    private List<NonConsumableMaterialTypeDto> materialTypes;
    private Long powerSourceId;
    private String powerSourceName;
    private String warrantyPeriod;
    private boolean serviceAvailability;
    //    private String countryOfOrigin;
    private Long countryId;
    private String countryName;
    private String manufacturerName;
    //    private String storageCondition;
    private Long storageConditionId;
    private String storageConditionName;
    private String brochurePath;
}
