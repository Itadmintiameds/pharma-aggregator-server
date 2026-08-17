package com.example.pharmaaggregatorserver.dto.order;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * One cart line that could not be fulfilled during a partial-success order
 * placement (e.g. insufficient stock) — see OrderPlacementServiceImpl. The
 * rest of the request's fulfillable lines still get placed as normal
 * SellerOrders; this only reports what got dropped so the buyer can act on it
 * (retry without this item, wait for restock, etc).
 */
@Getter
@AllArgsConstructor
public class RejectedLineDTO {
    private String productId;
    private String reason;
}
