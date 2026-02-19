package com.example.pharmaaggregatorserver.service.product;

import com.example.pharmaaggregatorserver.dto.product.CreateProductRequestDto;
import com.example.pharmaaggregatorserver.dto.product.ProductDetailsDrugDto;
import com.example.pharmaaggregatorserver.entity.product.ProductDetailsDrug;

import java.util.List;

public interface ProductDetailsDrugService {

    public ProductDetailsDrugDto createProduct(CreateProductRequestDto request);

    List<ProductDetailsDrugDto> getAllProducts();

    ProductDetailsDrugDto getProductById(Long productId);

    void deleteProduct(Long productId);

    ProductDetailsDrug updateProduct(
            Long productId,
            ProductDetailsDrugDto dto
    );


}
