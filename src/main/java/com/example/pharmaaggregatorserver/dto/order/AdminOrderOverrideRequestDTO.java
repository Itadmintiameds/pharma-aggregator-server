package com.example.pharmaaggregatorserver.dto.order;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Body for POST /admin/orders/{orderId}/override — force-transitions every
 * SellerOrder under the given Order to newStatus, bypassing the legal
 * seller-fulfilment transition check (still writes history, changedByRole
 * ADMIN).
 */
@Getter
@Setter
public class AdminOrderOverrideRequestDTO {

    @NotBlank(message = "newStatus is required")
    private String newStatus;

    @NotBlank(message = "reason is required")
    private String reason;

    private String adminId;
}
