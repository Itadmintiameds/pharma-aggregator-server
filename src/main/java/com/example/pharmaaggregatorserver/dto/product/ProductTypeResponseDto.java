package com.example.pharmaaggregatorserver.dto.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ProductTypeResponseDto {

    private String productId;
    private List<String> productTypeNames;
}
