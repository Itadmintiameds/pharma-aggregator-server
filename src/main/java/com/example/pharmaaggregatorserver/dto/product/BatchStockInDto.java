package com.example.pharmaaggregatorserver.dto.product;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BatchStockInDto {

    // Optional — which packaging/pack-size variant this batch line applies to.
    private String packagingId;
    private String batchLotNumber;
    private LocalDateTime manufacturingDate;
    private LocalDateTime expiryDate;
    private Long quantity;
    private BigDecimal mrp;
    private BigDecimal sellingPrice;
    private String referenceId;
    private String referenceType;
}
