package com.example.pharmaaggregatorserver.entity.temp.buyer;

/**
 * Status string constants for {@link TempBuyer#getStatus()}. Deliberately
 * NOT a JPA {@code @Enumerated} enum — the column stays a plain
 * {@code varchar}; mirrors {@code entity.temp.seller.TempSellerStatus}.
 * <p>
 * All status comparisons use {@code equalsIgnoreCase}.
 */
public final class TempBuyerStatus {

    public static final String DRAFT = "DRAFT";
    public static final String SUBMITTED = "SUBMITTED";
    public static final String UNDER_REVIEW = "UNDER_REVIEW";
    public static final String APPROVED = "APPROVED";
    public static final String CORRECTION_REQUIRED = "CORRECTION_REQUIRED";
    public static final String REJECTED = "REJECTED";
    public static final String SUSPENDED = "SUSPENDED";

    private TempBuyerStatus() {
    }
}
