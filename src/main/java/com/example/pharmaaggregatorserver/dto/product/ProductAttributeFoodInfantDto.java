package com.example.pharmaaggregatorserver.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductAttributeFoodInfantDto {

    private String productAttributeId;
    private Long productCategoryId;
    private Long productSubcategoryId;
    private String brandName;
    private String variantName;
    private Long productFormId;
    private String netQuantity;
    private String servingSize;
    private Long ageGroupId;
    private String vegNonvegIndicator;
    private String allergenInformation;
    private String nutritionalInformation;
    private String nutritionalInformationImageUrl;
    private String activeIngredients;
    private String additivesPreservatives;
    private String productClaims;
    private Long storageConditionId;
    private Long countryId;
    private List<Long> certificationIds;
    private List<ProductCertificateDocumentDto> certificateDocuments;
//    private String userManualUrl;
}
