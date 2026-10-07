package com.example.pharmaaggregatorserver.dto.order;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReturnResponseDTO {
    private Long returnId;
    private Long orderItemId;
    private String buyerId;
    private String reason;
    private String status;
    private LocalDateTime requestedAt;
    private LocalDateTime resolvedAt;
    private String resolvedByRole;
    private Long refundId;
}
