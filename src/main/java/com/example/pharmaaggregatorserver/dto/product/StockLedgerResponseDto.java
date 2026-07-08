package com.example.pharmaaggregatorserver.dto.product;

import com.example.pharmaaggregatorserver.enums.StockTransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockLedgerResponseDto {

    private Long ledgerId;
    private String pricingId;
    private String batchLotNumber;
    private String productId;
    private StockTransactionType transactionType;
    private Long quantity;
    private Long balanceAfter;
    private String referenceId;
    private String referenceType;
    private LocalDateTime createdDate;
}
