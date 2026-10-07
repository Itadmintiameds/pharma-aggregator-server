package com.example.pharmaaggregatorserver.entity.order;

/**
 * Status string constants for {@link ReturnRequest#getStatus()}. Deliberately
 * NOT a JPA {@code @Enumerated} enum — mirrors
 * {@code entity.temp.buyer.TempBuyerStatus}.
 */
public final class ReturnStatus {

    public static final String REQUESTED = "REQUESTED";
    public static final String APPROVED = "APPROVED";
    public static final String REJECTED = "REJECTED";
    public static final String PICKED_UP = "PICKED_UP";
    public static final String CLOSED = "CLOSED";

    private ReturnStatus() {
    }
}
