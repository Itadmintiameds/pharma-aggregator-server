package com.example.pharmaaggregatorserver.entity.order;

/**
 * Status string constants for {@link SellerOrder#getStatus()}. Deliberately
 * NOT a JPA {@code @Enumerated} enum — mirrors
 * {@code entity.temp.buyer.TempBuyerStatus}.
 */
public final class SellerOrderStatus {

    public static final String PLACED = "PLACED";
    public static final String CONFIRMED = "CONFIRMED";
    public static final String PACKED = "PACKED";
    public static final String SHIPPED = "SHIPPED";
    public static final String OUT_FOR_DELIVERY = "OUT_FOR_DELIVERY";
    public static final String DELIVERED = "DELIVERED";
    public static final String CANCELLED = "CANCELLED";
    public static final String RETURN_REQUESTED = "RETURN_REQUESTED";
    public static final String RETURN_APPROVED = "RETURN_APPROVED";
    public static final String RETURN_REJECTED = "RETURN_REJECTED";
    public static final String RETURNED = "RETURNED";
    public static final String REFUNDED = "REFUNDED";

    private SellerOrderStatus() {
    }
}
