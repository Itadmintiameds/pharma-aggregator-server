package com.example.pharmaaggregatorserver.dto.product;

import com.example.pharmaaggregatorserver.entity.product.ProductDetails;
import com.example.pharmaaggregatorserver.entity.product.TherapeuticCategoryMaster;
import lombok.Data;

import java.util.Set;

@Data
public class CategoryDto {

    private Long categoryId;
    private String categoryName;
    private String categoryDescription;
    private Set<TherapeuticCategoryMaster> therapeuticCategoryMasters;
    private Set<ProductDetails> productDetails;

}
