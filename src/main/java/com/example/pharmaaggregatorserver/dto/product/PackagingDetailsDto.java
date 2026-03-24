package com.example.pharmaaggregatorserver.dto.product;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PackagingDetailsDto {

    private String packagingId;
    private String packagingUnit;
    private Long numberOfUnits;
    private Long packSize;
    private Long minimumOrderQuantity;
    private Long maximumOrderQuantity;
    private String createdBy;
    private String modifiedBy;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
}
