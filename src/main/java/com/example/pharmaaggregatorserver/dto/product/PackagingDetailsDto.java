package com.example.pharmaaggregatorserver.dto.product;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PackagingDetailsDto {

    private String packagingId;
    private Long packId;
    private Long unitPerPack;
    private Long packTypeUnitId;
    private PackTypeUnitMasterDto packTypeUnitMasterDto;
    private Long numberOfPacks;
    private Long packSize;
    private Long minimumOrderQuantity;
    private Long maximumOrderQuantity;
    private String createdBy;
    private String modifiedBy;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;


}
