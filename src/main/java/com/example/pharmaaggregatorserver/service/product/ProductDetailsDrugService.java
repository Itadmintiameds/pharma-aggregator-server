package com.example.pharmaaggregatorserver.service.product;

import com.example.pharmaaggregatorserver.dto.product.ProductDetailsDrugDto;
import com.example.pharmaaggregatorserver.entity.product.ProductDetailsDrug;

import java.util.List;

public interface ProductDetailsDrugService {

    ProductDetailsDrugDto createProduct(ProductDetailsDrugDto dto);

    List<ProductDetailsDrugDto> getAllProducts();

    ProductDetailsDrugDto getProductById(Long productId);

    void deleteProduct(Long productId);

    ProductDetailsDrug updateProduct(
            Long productId,
            ProductDetailsDrugDto dto
    );


}
