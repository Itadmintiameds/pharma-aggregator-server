package com.example.pharmaaggregatorserver.dto.product;

import lombok.Data;

import java.util.List;

@Data
public class MultiBatchStockInRequestDto {

    private String productId;
    private List<BatchStockInDto> batches;
}
