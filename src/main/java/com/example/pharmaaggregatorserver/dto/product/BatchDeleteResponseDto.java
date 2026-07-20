package com.example.pharmaaggregatorserver.dto.product;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BatchDeleteResponseDto {

    private String pricingId;
    private String batchLotNumber;
    private String productId;
    private String deletedBy;
    private LocalDateTime deletedAt;
}
