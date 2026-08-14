package com.example.pharmaaggregatorserver.entity.order;

/**
 * Status string constants for {@link Order#getStatus()}. Deliberately NOT a
 * JPA {@code @Enumerated} enum — the column stays a plain {@code varchar};
 * mirrors {@code entity.temp.buyer.TempBuyerStatus}.
 * <p>
 * This is the parent-order rollup status, derived from the statuses of its
 * child {@link SellerOrder} rows (see OrderCancellationServiceImpl's rollup
 * helper) rather than being set directly in most cases.
 */
public final class OrderStatus {

    public static final String PLACED = "PLACED";
    public static final String PARTIALLY_SHIPPED = "PARTIALLY_SHIPPED";
    public static final String SHIPPED = "SHIPPED";
    public static final String DELIVERED = "DELIVERED";
    public static final String CANCELLED = "CANCELLED";

    private OrderStatus() {
    }
}
