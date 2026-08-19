package com.example.pharmaaggregatorserver.dto.quote;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class QuoteRequestResponseDTO {
    private Long quoteRequestId;
    private String requestType;
    private String status;

    private String productId;
    private String productName;
    private String sellerId;
    private String sellerName;

    private Integer quantity;
    private String unit;
    private BigDecimal targetPrice;
    private String pincode;
    private String deliveryLocation;
    private LocalDate expectedDeliveryDate;
    private String paymentTerms;
    private String companyName;
    private String gstNumber;
    private String contactPerson;
    private String phone;
    private String email;
    private String message;

    private BigDecimal quotedPrice;
    private LocalDate quoteValidUntil;
    private String sellerNotes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
