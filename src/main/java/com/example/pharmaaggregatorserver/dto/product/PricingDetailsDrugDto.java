package com.example.pharmaaggregatorserver.dto.product;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PricingDetailsDrugDto {

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
    private Long hsnCode;
    private Long createdBy;
    private Long modifiedBy;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
}
