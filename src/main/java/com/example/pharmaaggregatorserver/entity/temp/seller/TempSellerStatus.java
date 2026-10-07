package com.example.pharmaaggregatorserver.entity.temp.seller;

/**
 * Status string constants for {@link TempSeller#getStatus()}. Deliberately
 * NOT a JPA {@code @Enumerated} enum — the column stays a plain
 * {@code varchar} to avoid a wider migration; these constants just remove
 * the ad hoc string literals scattered across the service layer.
 * <p>
 * All existing status comparisons use {@code equalsIgnoreCase}, so using
 * these (uppercase) constants is safe/behavior-preserving even where the
 * persisted value was historically lowercase (e.g. "open").
 */
public final class TempSellerStatus {

    public static final String DRAFT = "DRAFT";
    public static final String OPEN = "OPEN";
    public static final String RESUBMITTED = "RESUBMITTED";
    public static final String APPROVED = "APPROVED";
    public static final String REJECTED = "REJECTED";
    public static final String CORRECTION_REQUIRED = "CORRECTION_REQUIRED";

    private TempSellerStatus() {
    }
}
