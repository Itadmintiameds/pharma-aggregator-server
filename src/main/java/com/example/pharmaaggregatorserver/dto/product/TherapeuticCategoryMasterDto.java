package com.example.pharmaaggregatorserver.dto.product;

import com.example.pharmaaggregatorserver.entity.product.TherapeuticSubcategoryMaster;
import lombok.Data;

import java.util.Set;

@Data
public class TherapeuticCategoryMasterDto {

    private String therapeuticCategoryId;
    private String therapeuticCategory;
    private Set<TherapeuticSubcategoryMaster> therapeuticSubcategoryMasters;


}
