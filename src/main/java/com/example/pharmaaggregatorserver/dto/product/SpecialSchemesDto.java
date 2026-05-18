package com.example.pharmaaggregatorserver.dto.product;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class SpecialSchemesDto {

    private String specialSchemesId;
    private String schemeName;
//    private String schemeType;
    private Long buyQuantity;
    private Long freeQuantity;
    private LocalDate effectiveStartDate;
    private LocalTime effectiveStartTime;
    private LocalDate effectiveEndDate ;
    private LocalTime effectiveEndTime;
    private String pricingId;
}
