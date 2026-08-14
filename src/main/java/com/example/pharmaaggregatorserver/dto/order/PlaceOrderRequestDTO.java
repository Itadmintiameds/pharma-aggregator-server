package com.example.pharmaaggregatorserver.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Buyer-facing order placement request. No Spring Security principal exists
 * for buyers yet (see entity.buyer.BuyerUser / TempBuyerRequestDTO's own
 * comment on this), so buyerId is passed explicitly rather than resolved
 * from a SecurityContext.
 * <p>
 * Delivery address is accepted as raw snapshot fields directly in the body
 * (simpler option per the task spec) rather than requiring a lookup against
 * the not-yet-CRUD'd {@code BuyerDeliveryAddress} table. If
 * {@code deliveryAddressId} is supplied it takes precedence and the address
 * fields below are ignored (see OrderPlacementServiceImpl).
 */
@Getter
@Setter
public class PlaceOrderRequestDTO {

    @NotBlank(message = "buyerId is required")
    private String buyerId;

    // Optional: if present, resolves against tbl_buyer_delivery_address and
    // overrides the raw fields below.
    private Long deliveryAddressId;

    private String deliveryName;
    private String deliveryPhone;
    private String deliveryAddressLine;
    private String deliveryCity;
    private String deliveryDistrict;
    private String deliveryState;
    private String deliveryPinCode;

    // "COD" or any other value meaning an online-payment order (PaymentService
    // webhook path); case-insensitive.
    private String paymentMethod;

    @NotEmpty(message = "At least one cart line is required")
    @Valid
    private List<OrderLineRequestDTO> lines;
}
