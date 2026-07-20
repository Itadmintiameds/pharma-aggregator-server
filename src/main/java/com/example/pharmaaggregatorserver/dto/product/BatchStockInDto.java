package com.example.pharmaaggregatorserver.dto.product;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BatchStockInDto {

    // Optional — which packaging/pack-size variant this batch line applies to.
    private String packagingId;
    // Optional — submit new packaging details to create/reuse a variant in the same call,
    // instead of referencing an existing packagingId. Ignored if packagingId is set.
    private PackagingDetailsDto packagingDetails;
    private String batchLotNumber;
    private LocalDateTime manufacturingDate;
    private LocalDateTime expiryDate;
    private Long quantity;
    private BigDecimal mrp;
    private BigDecimal sellingPrice;
    // Optional — regular discount percentage on this batch (0-100). Only applies when creating
    // a new batch; ignored when restocking an existing one.
    private BigDecimal discountPercentage;
    // Optional — one or more separate special-offer/promotional discounts, distinct from
    // discountPercentage. No new column: each entry is stored as its own AdditionalDiscount
    // record linked to the batch (reusing the existing tm_additional_discount table/relation)
    // instead of a PricingDetails field. Caller supplies minimumPurchaseQuantity,
    // additionalDiscountPercentage, and displayOffer per entry (pricingId/additionalDiscountId
    // are ignored — set server-side). Only applies when creating a new batch.
    private List<AdditionalDiscountDto> specialDiscounts;
    // Optional — shelf life of this batch in months. Only applies when creating a new batch.
    private Long shelfLifeMonths;
    // Optional — defaults to today (server-side) if omitted. Only applies when creating a new batch.
    private LocalDate dateOfStockEntry;
    private String referenceId;
    private String referenceType;
}
