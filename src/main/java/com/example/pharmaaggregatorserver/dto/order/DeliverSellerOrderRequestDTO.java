package com.example.pharmaaggregatorserver.dto.order;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Body for PATCH /seller-orders/{id}/deliver. Unlike the other fulfilment
 * transitions, marking DELIVERED requires the delivery OTP sent to the buyer
 * when the order moved to OUT_FOR_DELIVERY (see SellerOrderFulfillmentServiceImpl)
 * — proof the buyer actually received the order, not just a seller-side status flip.
 */
@Getter
@Setter
public class DeliverSellerOrderRequestDTO {

    @NotBlank(message = "sellerId is required")
    private String sellerId;

    @NotBlank(message = "otp is required to confirm delivery")
    private String otp;
}
