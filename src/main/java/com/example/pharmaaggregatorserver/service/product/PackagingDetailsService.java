package com.example.pharmaaggregatorserver.service.product;

import com.example.pharmaaggregatorserver.entity.product.PackagingDetails;
import com.example.pharmaaggregatorserver.entity.product.ProductDetails;

public interface PackagingDetailsService {

    /**
     * Finds-or-creates a packaging/pack-size variant under the given product, matched by
     * pack type + unitPerPack + numberOfPacks + min/maxOrderQuantity on {@code candidate}.
     *
     * If a matching variant already exists on this product, it's reused as-is (candidate's
     * fields are discarded). Otherwise {@code candidate} itself is stamped with a new ID and
     * audit fields, then returned as the new variant.
     *
     * This is the single shared implementation used by product creation/update and stock-in,
     * so "is this the same variant or a new one" is decided the same way everywhere.
     */
    PackagingDetails resolveOrCreatePackaging(
            ProductDetails product,
            PackagingDetails candidate,
            String sellerName,
            String sellerId
    );

}
