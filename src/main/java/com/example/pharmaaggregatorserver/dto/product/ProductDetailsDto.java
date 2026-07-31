package com.example.pharmaaggregatorserver.dto.product;

import com.example.pharmaaggregatorserver.entity.product.PackagingDetails;
import com.example.pharmaaggregatorserver.entity.product.PricingDetails;
import com.example.pharmaaggregatorserver.entity.product.ProductAttributeDrug;
import com.example.pharmaaggregatorserver.entity.product.ProductImage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
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
    @Pattern(regexp = "^[a-zA-Z0-9\\s!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]*$",
            message = "Manufacturer Name can contain alphanumeric and special characters")
    private String manufacturerName;
    private String createdBy;
    private String modifiedBy;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private Long categoryId;
    private Set<PackagingDetailsDto> packagingDetails;
    private Set<PricingDetailsDto> pricingDetails;
    @Valid
    private Set<ProductAttributeDrugDto> productAttributeDrugs;
    @Valid
    private Set<ConsumableProductAttributeDTO> productAttributeConsumableMedicals;
    @Valid
    private Set<ProductAttributeNonConsumableMedicalDto> productAttributeNonConsumableMedicals;
    @Valid
    private Set<ProductAttributeSupplementsOrNutraceuticalsDto> productAttributeSupplementsOrNutraceuticals;
    @Valid
    private Set<CosmeticAndPersonalUseProductAttributeDTO> productAttributeCosmeticAndPersonalUse;
    private Set<ProductImageDto> productImages;
    private List<String> retainedImageUrls;
    @Valid
    private Set<ProductAttributeFoodInfantDto> productAttributeFoodInfants;
    private Long gstPercentage;
    private Long hsnCode;
    private String status;

}
