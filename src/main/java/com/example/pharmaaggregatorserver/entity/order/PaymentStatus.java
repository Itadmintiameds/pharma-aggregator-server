package com.example.pharmaaggregatorserver.entity.order;

/**
 * Status string constants for {@link Payment#getStatus()}. Deliberately NOT
 * a JPA {@code @Enumerated} enum — mirrors
 * {@code entity.temp.buyer.TempBuyerStatus}.
 */
public final class PaymentStatus {

    public static final String INITIATED = "INITIATED";
    public static final String SUCCESS = "SUCCESS";
    public static final String FAILED = "FAILED";
    public static final String REFUNDED = "REFUNDED";
    public static final String PARTIALLY_REFUNDED = "PARTIALLY_REFUNDED";
    public static final String PENDING_COD = "PENDING_COD";

    private PaymentStatus() {
    }
}
