package com.example.pharmaaggregatorserver.dto.product;

import lombok.Data;

@Data
public class ConsumableProductAttributeDTO {
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
    private String countryOfOrigin;
    private String manufacturerName;
    private String storageCondition;
    private String brochureType;
    private String brochurePath;
}
