package com.example.pharmaaggregatorserver.dto.product;

import lombok.Data;

@Data
public class DrugCategoryDto {

    private String categoryId;
    private String categoryName;
    private String example;
    private String primaryUse;
}
