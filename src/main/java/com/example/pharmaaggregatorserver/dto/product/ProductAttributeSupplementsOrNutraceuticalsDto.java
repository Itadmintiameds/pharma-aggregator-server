package com.example.pharmaaggregatorserver.dto.product;

import com.example.pharmaaggregatorserver.entity.product.AgeGroupMaster;
import com.example.pharmaaggregatorserver.entity.product.Flavour;
import lombok.Data;

import java.util.List;

@Data
public class ProductAttributeSupplementsOrNutraceuticalsDto {

    private String productAttributeId;
    private String therapeuticCategoryId;
    private String therapeuticCategoryName;
    private String therapeuticSubCategoryId;
    private String therapeuticSubCategoryName;
    private String brandName;
    private String variantName;
    private Long dosageFormId;
    private String dosageFormName;
    private Double netQuantity;
    private Long netQuantityUnitId;
    private String netQuantityUnitName;
    private Double servingSize;
    private Long servingSizeUnitId;
    private String servingSizeUnitName;
    private String strength;
    private String activeIngredients;
    private String otherIngredients;
    private String nutritionalInformation;
    private String nutritionalInformationImageUrl;
    private String intendedUse;
    private Long ageGroupId;
    private String ageGroupName;
    private String gender;
    private String vegOrNonVegIndicator;
    private String allergenInformation;
    private Long flavourId;
    private String flavourName;
    private String productClaims;
    private Long storageConditionId;
    private String storageConditionName;
//    private String manufacturerName;
    private Long countryId;
    private String countryName;
    private List<ProductCertificateDocumentDto> certificateDocuments;
    private String brochurePath;
}
