package com.example.pharmaaggregatorserver.dto.order;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Flat projection of {@code SellerOrder} — never the raw entity.
 */
@Getter
@Builder
public class SellerOrderResponseDTO {
    private String sellerOrderId;
    private String orderId;
    // Set when the parent order was placed from an already-ACCEPTED quote
    // request (see OrderPlacementServiceImpl) — null for an ordinary cart
    // checkout. Lets the seller tell a negotiated-price order apart from a
    // regular one at a glance.
    private Long quoteRequestId;
    private String sellerId;
    private String status;
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal taxAmount;
    private BigDecimal grandTotal;
    private String courierName;
    private String trackingNumber;
    private String trackingUrl;
    private LocalDateTime confirmedAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime cancelledAt;
    private String cancelReason;
    private String cancelledByRole;
    private Long invoiceId;
    private List<OrderItemResponseDTO> items;
    private List<OrderStatusHistoryResponseDTO> statusHistory;
    private LocalDateTime createdAt;
}
