package com.example.pharmaaggregatorserver.dto.product;

import com.example.pharmaaggregatorserver.entity.product.PackType;
import com.example.pharmaaggregatorserver.entity.product.TherapeuticCategoryMaster;
import lombok.Data;

import java.util.Set;

@Data
public class DosageFormDto {

    private Long dosageId;
    private String dosageName;
    private Set<PackTypeDto> packTypes;


}
