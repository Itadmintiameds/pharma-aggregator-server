package com.example.pharmaaggregatorserver.dto.product;

import com.example.pharmaaggregatorserver.entity.product.ProductUserManual;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

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
    private String userManualUrl;
}
