package com.example.pharmaaggregatorserver.dto.product;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class ProductDetailsDrugDto {

    private String productId;
    private String productCategoryId;
    private String productName;
    private String therapeuticCategory;
    private String therapeuticSubcategory;
    private String dosageForm;
    private Long strength;
    private String warningsPrecautions;
    private String productDescription;
    private String productImage;
    private String productMarketingUrl;
    private Long createdBy;
    private Long modifiedBy;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;

    private Set<MoleculeDto> molecules;
    private PackagingDetailsDrugDto packagingDetails;
    private Set<PricingDetailsDrugDto> pricingDetails;
}
