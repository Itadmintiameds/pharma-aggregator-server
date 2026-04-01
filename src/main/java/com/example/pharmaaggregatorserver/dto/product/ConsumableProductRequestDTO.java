package com.example.pharmaaggregatorserver.dto.product;

import lombok.Data;

import java.util.List;

@Data
public class ConsumableProductRequestDTO {

    // ✅ ADD THESE
    private Long categoryId;
    private String sellerId;
    private String productName;
    private String productDescription;
    private String warningsPrecautions;

    private ConsumableProductAttributeDTO productAttributes;
    private PackagingDetailsDto packaging;
    private List<PricingDetailsDto> pricing;
    private List<ProductImageDto> productImages;
}
