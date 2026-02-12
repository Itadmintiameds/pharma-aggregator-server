package com.example.pharmaaggregatorserver.dto.product;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PackagingDetailsDrugDto {

    private Long packagingId;
    private String packagingUnit;
    private Long numberOfUnits;
    private Long packSize;
    private Long minimumOrderQuantity;
    private Long maximumOrderQuantity;
    private Long createdBy;
    private Long modifiedBy;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
}
