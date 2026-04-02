package com.example.pharmaaggregatorserver.dto.product;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductAttributeDrugDto {

    private String productAttributeId;
    private String therapeuticCategoryId;
    private String therapeuticSubcategoryId;
    private String dosageForm;
    private String createdBy;
    private String modifiedBy;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private List<ProductMoleculeDto> molecules;

}
