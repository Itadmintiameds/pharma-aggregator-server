package com.example.pharmaaggregatorserver.service.product;

import com.example.pharmaaggregatorserver.dto.product.ProductDetailsDto;

public interface ProductDetailsService {

    ProductDetailsDto createProduct(ProductDetailsDto dto, Long userId);
}
