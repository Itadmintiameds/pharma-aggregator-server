package com.example.pharmaaggregatorserver.dto.product;

import com.example.pharmaaggregatorserver.entity.product.AdditionalDiscount;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class PricingDetailsDto {

    private String pricingId;
    private String batchLotNumber;
    private String manufacturerName;
    private LocalDateTime manufacturingDate;
    private LocalDateTime expiryDate;
    private String storageCondition;
    private Long stockQuantity;
    private Long pricePerUnit;
    private Long mrp;
    private Long discountPercentage;
    private Long gstPercentage;
    private Long minimumPurchaseQuantity;
    private Long additionalDiscount;
    private Long finalPrice;
    private Long hsnCode;
    private Long shelfLifeMonths;
    private String createdBy;
    private String modifiedBy;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private Set<AdditionalDiscountDto> additionalDiscounts;

}
