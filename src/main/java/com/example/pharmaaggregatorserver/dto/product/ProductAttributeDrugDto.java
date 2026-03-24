package com.example.pharmaaggregatorserver.dto.product;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProductAttributeDrugDto {

    private String productAttributeId;
    private String therapeuticCategoryId;
    private String therapeuticSubcategoryId;
    private String dosageForm;
    private String strength;
    private String createdBy;
    private String modifiedBy;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
}
