package com.example.pharmaaggregatorserver.dto.product;

import com.example.pharmaaggregatorserver.entity.product.ProductSubcategoryMaster;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductCategoryMasterDto {

    private Long productCategoryId;
    private String productCategory;
    private Long categoryId;
    private Set<ProductSubcategoryMasterDto> productSubcategoryMasters;
}
