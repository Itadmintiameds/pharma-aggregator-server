package com.example.pharmaaggregatorserver.dto.product;

import lombok.Data;

import java.util.Set;

@Data
public class DosageFormDto {

    private Long dosageId;
    private String dosageName;
    private Long categoryId;
    private String categoryName;
    private Set<PackTypeDto> packTypes;


}
