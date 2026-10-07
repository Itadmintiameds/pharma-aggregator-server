package com.example.pharmaaggregatorserver.dto.order;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Flat projection of {@code Order} — never the raw entity, so nothing from
 * Buyer/BuyerUser (passwordHash etc.) can leak through a response.
 */
@Getter
@Builder
public class OrderResponseDTO {
    private String orderId;
    private String buyerId;
    private String deliveryName;
    private String deliveryPhone;
    private String deliveryAddressLine;
    private String deliveryCity;
    private String deliveryDistrict;
    private String deliveryState;
    private String deliveryPinCode;
    private String status;
    // Set when this order was placed from an already-ACCEPTED quote request
    // (see OrderPlacementServiceImpl) — null for an ordinary cart checkout.
    private Long quoteRequestId;
    private Integer itemCount;
    private Integer sellerOrderCount;
    private BigDecimal subtotal;
    private BigDecimal shippingTotal;
    private BigDecimal taxTotal;
    private BigDecimal grandTotal;
    private String paymentId;
    private String paymentStatus;
    private LocalDateTime placedAt;
    private LocalDateTime cancelledAt;
    private String cancelledByRole;
    private String cancelReason;
    private List<SellerOrderResponseDTO> sellerOrders;
    private LocalDateTime createdAt;
    // Non-empty only on the response to POST /orders when the order placed
    // as a partial success — lines dropped for insufficient stock while the
    // rest of the cart still got placed. Empty for every other order read.
    private List<RejectedLineDTO> rejectedLines;
}
