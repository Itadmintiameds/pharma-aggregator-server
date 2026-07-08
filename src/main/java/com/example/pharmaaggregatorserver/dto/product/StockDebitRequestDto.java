package com.example.pharmaaggregatorserver.dto.product;

import lombok.Data;

@Data
public class StockDebitRequestDto {

    private String productId;
    private Long quantity;
    private String referenceId;
    private String referenceType;
}
