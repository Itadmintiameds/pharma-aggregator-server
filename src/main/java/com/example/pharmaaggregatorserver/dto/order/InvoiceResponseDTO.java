package com.example.pharmaaggregatorserver.dto.order;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class InvoiceResponseDTO {
    private Long invoiceId;
    private String sellerOrderId;
    private String invoiceNumber;
    private String invoiceFileUrl;
    private LocalDateTime generatedAt;
}
