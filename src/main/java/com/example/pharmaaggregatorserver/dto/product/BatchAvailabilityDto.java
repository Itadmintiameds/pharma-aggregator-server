package com.example.pharmaaggregatorserver.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchAvailabilityDto {

    private String pricingId;
    private String packagingId;
    private String batchLotNumber;
    private LocalDateTime manufacturingDate;
    private LocalDateTime expiryDate;
    private Long stockQuantity;
    private BigDecimal discountPercentage;
    private Long shelfLifeMonths;
    private LocalDate dateOfStockEntry;
}
