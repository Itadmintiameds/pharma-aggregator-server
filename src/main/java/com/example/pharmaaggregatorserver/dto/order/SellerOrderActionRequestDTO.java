package com.example.pharmaaggregatorserver.dto.order;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Body for the simple seller-fulfilment transitions (confirm/pack/
 * out-for-delivery/deliver) that only need the acting seller's identity for
 * the ownership check.
 */
@Getter
@Setter
public class SellerOrderActionRequestDTO {

    @NotBlank(message = "sellerId is required")
    private String sellerId;
}
