package com.example.pharmaaggregatorserver.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class ConsumableProductAttributeDTO {
    private String productAttributeId;
    private Long deviceCatId;
    private String brandName;
    private Long materialTypeId;
    private Long diamensionId;
    private String sterileOrNonSterile;
    private String disposalOrReusable;
    private String shelfLife;
    private String purpose;
    private String keyFeaturesSpecifications;
    private Long certificationId;
    private String certificationName;
    private String complianceCertificateUrl;
    private String countryOfOrigin;
    private String manufacturerName;
    private String storageCondition;
    private String brochureType;
    private String brochurePath;
}
