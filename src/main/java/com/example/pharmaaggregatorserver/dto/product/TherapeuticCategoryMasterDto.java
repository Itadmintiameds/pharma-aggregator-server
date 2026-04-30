package com.example.pharmaaggregatorserver.dto.product;

import com.example.pharmaaggregatorserver.entity.product.TherapeuticSubcategoryMaster;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TherapeuticCategoryMasterDto {

    private String therapeuticCategoryId;
    private String therapeuticCategory;
    private Long categoryId;
    private String categoryName;
    private Set<TherapeuticSubcategoryMaster> therapeuticSubcategoryMasters;

    public TherapeuticCategoryMasterDto(String id, String name, Long categoryId, String categoryName) {
        this.therapeuticCategoryId = id;
        this.therapeuticCategory = name;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

}
