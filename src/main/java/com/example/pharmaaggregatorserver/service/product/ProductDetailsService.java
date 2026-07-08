package com.example.pharmaaggregatorserver.service.product;

import com.example.pharmaaggregatorserver.dto.product.ProductDetailsDto;
import com.example.pharmaaggregatorserver.dto.product.TherapeuticSubcategoryDto;

import java.util.List;

public interface ProductDetailsService {

    ProductDetailsDto createProduct(ProductDetailsDto dto, Long userId);

    List<ProductDetailsDto> getAllProducts(Long userId);

    List<ProductDetailsDto> getAllProductsForAdmin();
    ProductDetailsDto getProductById(String productId, Long userId);

    void deleteProductById(String productId, Long userId);

    ProductDetailsDto updateProduct(String productId, ProductDetailsDto dto, Long userId);

    List<TherapeuticSubcategoryDto> getSubcategories(String categoryId);


}
