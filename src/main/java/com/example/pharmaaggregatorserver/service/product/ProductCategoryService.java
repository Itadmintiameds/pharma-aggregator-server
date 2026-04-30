package com.example.pharmaaggregatorserver.service.product;

import com.example.pharmaaggregatorserver.dto.product.ProductCategoryMasterDto;
import com.example.pharmaaggregatorserver.dto.product.ProductSubcategoryMasterDto;

import java.util.List;

public interface ProductCategoryService {

    List<ProductCategoryMasterDto> getProductCategoriesByCategoryId(Long categoryId);

    List<ProductSubcategoryMasterDto> getByProductCategoryId(Long productCategoryId);

}
