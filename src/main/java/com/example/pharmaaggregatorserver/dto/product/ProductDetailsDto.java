package com.example.pharmaaggregatorserver.dto.product;

import com.example.pharmaaggregatorserver.entity.product.PackagingDetails;
import com.example.pharmaaggregatorserver.entity.product.PricingDetails;
import com.example.pharmaaggregatorserver.entity.product.ProductAttributeDrug;
import com.example.pharmaaggregatorserver.entity.product.ProductImage;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
public class ProductDetailsDto {

    private String productId;
    private String productName;
    private String warningsPrecautions;
    private String productDescription;
    private String manufacturerName;
    private String createdBy;
    private String modifiedBy;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private Long categoryId;
    private Set<PackagingDetailsDto> packagingDetails;
    private Set<PricingDetailsDto> pricingDetails;
    private Set<ProductAttributeDrugDto> productAttributeDrugs;
    private Set<ConsumableProductAttributeDTO> productAttributeConsumableMedicals;
    private Set<ProductAttributeNonConsumableMedicalDto> productAttributeNonConsumableMedicals;
    private Set<ProductAttributeSupplementsOrNutraceuticalsDto> productAttributeSupplementsOrNutraceuticals;
    private Set<CosmeticAndPersonalUseProductAttributeDTO> productAttributeCosmeticAndPersonalUse;
    private Set<ProductImageDto> productImages;
    private List<String> retainedImageUrls;
    private Set<ProductAttributeFoodInfantDto> productAttributeFoodInfants;
    private Long gstPercentage;
    private Long hsnCode;
    private String status;

}
