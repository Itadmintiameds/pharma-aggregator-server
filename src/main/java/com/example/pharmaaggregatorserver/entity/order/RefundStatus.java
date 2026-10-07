package com.example.pharmaaggregatorserver.entity.order;

/**
 * Status string constants for {@link Refund#getStatus()}. Deliberately NOT a
 * JPA {@code @Enumerated} enum — mirrors
 * {@code entity.temp.buyer.TempBuyerStatus}.
 */
public final class RefundStatus {

    public static final String REQUESTED = "REQUESTED";
    public static final String PROCESSING = "PROCESSING";
    public static final String COMPLETED = "COMPLETED";
    public static final String FAILED = "FAILED";

    private RefundStatus() {
    }
}
