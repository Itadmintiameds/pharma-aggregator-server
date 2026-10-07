package com.example.pharmaaggregatorserver.service.product;

import com.example.pharmaaggregatorserver.entity.product.PackagingDetails;
import com.example.pharmaaggregatorserver.entity.product.PricingDetails;
import com.example.pharmaaggregatorserver.entity.product.ProductDetails;

public interface PricingDetailsService {

    boolean isBatchNumberExistsForSeller(String batchLotNumber, Long sellerId, Long categoryId);

    /**
     * Finds-or-creates a batch for the given product (and, when provided, the given packaging
     * variant), matched by {@code candidate.getBatchLotNumber()}.
     *
     * If a batch with this lot number already exists in that scope, its stockQuantity is
     * incremented by {@code candidate.getStockQuantity()} (a restock) — but only if the expiry
     * date matches, otherwise a lot-number reuse mistake is assumed and rejected. The existing
     * row is returned; any extra fields/children already set on {@code candidate} (discounts,
     * schemes, etc.) are discarded since the real batch already carries its own.
     *
     * If no batch matches, {@code candidate} itself is stamped with an ID, product/packaging
     * references, and audit fields, then returned as the new batch — callers that pre-populate
     * {@code candidate} with child collections (additional discounts, special schemes) via a
     * mapper keep those intact, since it's the same object, not a copy.
     *
     * This is the single shared implementation of "add a batch" used by both product
     * creation/update and the dedicated stock-in/out endpoints, so the restock-vs-create
     * decision is made the same way everywhere.
     *
     * @param packaging nullable — null means the batch isn't linked to a specific variant
     */
    PricingDetails resolveOrCreateBatch(
            ProductDetails product,
            PackagingDetails packaging,
            PricingDetails candidate,
            String sellerName,
            String sellerId
    );

}
