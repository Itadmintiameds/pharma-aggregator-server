package com.example.pharmaaggregatorserver.dto.order;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Body for POST /orders/{orderId}/cancel and PATCH /seller-orders/{id}/cancel.
 * No Buyer/Seller security principal exists yet, so actor identity is passed
 * explicitly (mirrors PlaceOrderRequestDTO's own reasoning).
 */
@Getter
@Setter
public class CancelOrderRequestDTO {

    @NotBlank(message = "actorRole is required")
    private String actorRole; // BUYER / SELLER / ADMIN

    @NotBlank(message = "actorId is required")
    private String actorId;

    private String reason;
}
