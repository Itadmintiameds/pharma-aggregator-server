package com.example.pharmaaggregatorserver.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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
    private Long unitId;
    private Double servingSize;
    private Long servingSizeUnitId;
    private String servingSizeUnitName;
    private List<Long> ageGroupIds;
    private List<AgeGroupMasterDto> ageGroupMastersDto;
    private String vegNonvegIndicator;
    private String allergenInformation;
    private String nutritionalInformation;
    private String nutritionalInformationImageUrl;
    private String activeIngredients;
    private String additivesPreservatives;
    private String productClaims;
    private String productUserManual;
    private Long storageConditionId;
    private Long countryId;
    private List<Long> certificationIds;
    private List<ProductCertificateDocumentDto> certificateDocuments;
    private String createdBy;
    private String modifiedBy;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
}
