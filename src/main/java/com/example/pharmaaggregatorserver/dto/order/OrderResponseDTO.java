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
}
