package com.example.pharmaaggregatorserver.dto.order;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Flat projection of {@code OrderItem} — never the raw entity, so nothing
 * from the ProductDetails/PricingDetails/Seller chain can leak.
 */
@Getter
@Builder
public class OrderItemResponseDTO {
    private Long orderItemId;
    private String productId;
    private String pricingId;
    private String productNameSnapshot;
    private String batchLotNumberSnapshot;
    private String packagingIdSnapshot;
    private Integer quantity;
    private BigDecimal unitPriceSnapshot;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal lineTotal;
    private String itemStatus;
}
