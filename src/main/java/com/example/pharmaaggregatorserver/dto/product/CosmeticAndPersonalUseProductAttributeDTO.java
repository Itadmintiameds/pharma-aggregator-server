package com.example.pharmaaggregatorserver.dto.product;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
@Data
public class CosmeticAndPersonalUseProductAttributeDTO {
    private String productAttributeId;
    private Long productCategoryId;
    private Long productSubcategoryId;
    private String brandName;
    private String VariantName;
    private String Gender;

    // Changed from single Long to List for many-to-many
    private List<Long> useAreaId = new ArrayList<>();

    // Changed from single Long to List for many-to-many
    private List<Long> typeId = new ArrayList<>();

    // Changed from single Long to List for many-to-many
    private List<Long> skintypeId = new ArrayList<>();

    // For displaying IntendedArea names in response
    private List<IntendedAreaInfo> intendedarea = new ArrayList<>();

    // For displaying HairType names in response
    private List<HairTypeInfo> hairType = new ArrayList<>();

    // For displaying HairType names in response
    private List<SkinTypeInfo> skinType = new ArrayList<>();

    private String ActiveIngredients;
    private Long unitId;
    private String unitName;
    private BigDecimal NetQuantityStrength;
    //private Long ageGroupId;
    private List<Long> ageGroupIds;
    private List<AgeGroupInfo> ageGroupInfo;
    private String ProductClaims;

    //private String storageCondition;
    private Long storageConditionId;

    private String manufacturerName;
    private Long countryId;
    private Long formId;
    private String formName;

// Replaces single certificationId + certificationName + complianceCertificateUrl
    private List<ProductCertificateDocumentDto> certificateDocuments;

    private String brochurePath;

    // Inner class for Intended Area Info
    @Data
    public static class IntendedAreaInfo {
        private Long useAreaId;
        private String areaName;
    }

    //HairTypeInfo
    @Data
    public static class HairTypeInfo {
        private Long typeId;
        private String typeName;
    }

    //SkinTypeInfo
    @Data
    public static class SkinTypeInfo {
        private Long skintypeId;
        private String typeName;
    }

    //age group
    @Data
    public static class AgeGroupInfo {
        private Long ageGroupId;
        private String ageGroup;
    }


}
