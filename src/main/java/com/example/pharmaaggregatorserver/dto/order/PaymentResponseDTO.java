package com.example.pharmaaggregatorserver.dto.order;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentResponseDTO {
    private String paymentId;
    private String orderId;
    private String provider;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String providerOrderId;
    private String providerTransactionId;
    private LocalDateTime paidAt;
    private String failureReason;
}
