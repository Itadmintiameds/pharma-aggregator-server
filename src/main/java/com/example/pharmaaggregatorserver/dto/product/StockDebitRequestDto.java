package com.example.pharmaaggregatorserver.dto.product;

import lombok.Data;

@Data
public class StockDebitRequestDto {

    private String productId;
    // Optional — restrict FIFO debit to this specific packaging/pack-size variant only.
    private String packagingId;
    private Long quantity;
    private String referenceId;
    private String referenceType;
}
