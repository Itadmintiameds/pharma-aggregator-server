package com.example.pharmaaggregatorserver.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

/**
 * One cart line in a {@link PlaceOrderRequestDTO}. No sellerId here on
 * purpose — the seller is resolved server-side from
 * pricingId -> productDetails -> seller, never trusted from the client.
 */
@Getter
@Setter
public class OrderLineRequestDTO {

    @NotBlank(message = "productId is required")
    private String productId;

    @NotBlank(message = "pricingId is required")
    private String pricingId;

    @NotNull(message = "quantity is required")
    @Positive(message = "quantity must be positive")
    private Integer quantity;
}
