package com.example.pharmaaggregatorserver.service.product;

import com.example.pharmaaggregatorserver.dto.product.ConsumableProductRequestDTO;

public interface ConsumableProductService {
    String createFullProduct(ConsumableProductRequestDTO dto);
}
