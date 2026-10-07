package com.example.pharmaaggregatorserver.enums;

public enum QuoteRequestStatus {
    PENDING,
    QUOTED,
    ACCEPTED,
    REJECTED,
    EXPIRED,
    // Set once an ACCEPTED quote has been converted into an Order (see
    // OrderPlacementServiceImpl) — terminal, prevents the same accepted quote
    // from being placed as an order more than once.
    ORDER_PLACED
}
