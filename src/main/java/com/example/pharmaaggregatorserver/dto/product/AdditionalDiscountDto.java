package com.example.pharmaaggregatorserver.dto.product;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

@Data
public class AdditionalDiscountDto {

    private String additionalDiscountId;
    private Long minimumPurchaseQuantity;
    private BigDecimal additionalDiscountPercentage;
    private LocalDate effectiveStartDate;
    private LocalTime effectiveStartTime;
    private LocalDate effectiveEndDate ;
    private LocalTime effectiveEndTime;
    private Boolean displayOffer;
    private String pricingId;
}
