package com.example.pharmaaggregatorserver.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.util.ArrayList;
import java.util.List;

@Data
public class ConsumableProductAttributeDTO {
    private String productAttributeId;
    private Long deviceCatId;
    private Long deviceSubCatId;
    private String brandName;
   // private Long materialTypeId;

    // Changed from single Long to List for many-to-many
    private List<Long> materialTypeId = new ArrayList<>();

    // For displaying material type names in response
    private List<MaterialTypeInfo> materialTypes = new ArrayList<>();

    private Double dimensionSize;
    private Long deviceSpecificationUnitId;
    private String deviceSpecificationUnitName;
    private String sterileOrNonSterile;
    private String disposalOrReusable;
    private String shelfLife;
    private String purpose;
    private String keyFeaturesSpecifications;
    private String safetyInstructions;
//    private Long certificationId;
//    private String certificationName;
//    private String complianceCertificateUrl;
// Replaces single certificationId + certificationName + complianceCertificateUrl
    private List<ProductCertificateDocumentDto> certificateDocuments;

    private Long countryId;
    //private String countryOfOrigin;
    private String manufacturerName;
    //private String storageCondition;
    private Long storageConditionId;

    private String brochureType;
    private String brochurePath;


    // Inner class for material type info
    @Data
    public static class MaterialTypeInfo {
        private Long materialTypeId;
        private String materialTypeName;
    }
}
