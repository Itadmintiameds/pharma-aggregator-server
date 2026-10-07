package com.example.pharmaaggregatorserver.controller.product;

import com.example.pharmaaggregatorserver.dto.product.ProductCategoryMasterDto;
import com.example.pharmaaggregatorserver.dto.product.ProductSubcategoryMasterDto;
import com.example.pharmaaggregatorserver.service.product.ProductCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/productCategory")
@RequiredArgsConstructor
public class ProductCategoryController {

    private final ProductCategoryService productCategoryService;

    @GetMapping("/getProductCategory/{categoryId}")
    public List<ProductCategoryMasterDto> getProductCategoriesByCategoryId(
            @PathVariable Long categoryId) {

        return productCategoryService.getProductCategoriesByCategoryId(categoryId);
    }


    @GetMapping("/getProductSubcategory/{productCategoryId}")
    public List<ProductSubcategoryMasterDto> getByProductCategoryId(
            @PathVariable Long productCategoryId) {

        return productCategoryService.getByProductCategoryId(productCategoryId);
    }
}
