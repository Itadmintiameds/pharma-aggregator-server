package com.example.pharmaaggregatorserver.dto.product;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class PricingDetailsDto {

    private String pricingId;
    private String batchLotNumber;
    private LocalDateTime manufacturingDate;
    private LocalDateTime expiryDate;
    private Long stockQuantity;
    private LocalDate dateOfStockEntry;
    private BigDecimal sellingPrice;
    private BigDecimal mrp;
    private BigDecimal discountPercentage;
    private Long gstPercentage;
    private Long finalPrice;
    private Long hsnCode;
    private Long shelfLifeMonths;
    private String createdBy;
    private String modifiedBy;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private Set<AdditionalDiscountDto> additionalDiscounts;
    private Set<SpecialSchemesDto> specialSchemes;
}
