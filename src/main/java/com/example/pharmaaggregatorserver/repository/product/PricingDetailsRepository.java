package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.PricingDetails;
import com.example.pharmaaggregatorserver.entity.product.ProductDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;

public interface PricingDetailsRepository extends JpaRepository<PricingDetails, String>{

    @Query(value = """
    SELECT MAX(CAST(SUBSTRING(pricing_id, LENGTH(pricing_id) - 4, 5) AS INTEGER))
    FROM tm_pricing_details
""", nativeQuery = true)
    Integer findMaxPricingNumber();

    @Query("SELECT COALESCE(SUM(p.stockQuantity), 0) FROM PricingDetails p WHERE p.productDetails.productId = :productId")
    Long getTotalStockByProductId(@Param("productId") String productId);

    @Query("""
        SELECT COUNT(p) > 0
        FROM PricingDetails p
        WHERE p.batchLotNumber = :batchLotNumber
        AND p.productDetails.seller.user.userId = :userId
        AND p.productDetails.category.categoryId = :categoryId
    """)
    boolean existsByBatchLotNumberAndUserIdAndCategoryId(
            @Param("batchLotNumber") String batchLotNumber,
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId
    );

    // Locked lookup used by StockService to decide "restock existing batch" vs "create new batch"
    @Lock(PESSIMISTIC_WRITE)
    Optional<PricingDetails> findByProductDetails_ProductIdAndBatchLotNumber(String productId, String batchLotNumber);

    // Packaging-scoped variant of the above, used once a batch is linked to a specific
    // packaging/pack-size variant rather than the product as a whole.
    @Lock(PESSIMISTIC_WRITE)
    Optional<PricingDetails> findByProductDetails_ProductIdAndPackagingDetails_PackagingIdAndBatchLotNumber(
            String productId, String packagingId, String batchLotNumber);

    // Read-only, oldest-manufactured-first list used by the "view available batches" endpoint.
    // No lock: PESSIMISTIC_WRITE requires an active transaction, and this is called from
    // non-transactional read methods — locking here would also needlessly block concurrent debits.
    List<PricingDetails> findByProductDetails_ProductIdAndStockQuantityGreaterThanOrderByManufacturingDateAsc(
            String productId, Long minQty);

    // Packaging-scoped variant of the above, for viewing available batches of one specific variant.
    List<PricingDetails> findByProductDetails_ProductIdAndPackagingDetails_PackagingIdAndStockQuantityGreaterThanOrderByManufacturingDateAsc(
            String productId, String packagingId, Long minQty);

    // Locked variant used only inside StockService.debitStock's @Transactional FIFO allocation,
    // where holding the row lock across the read+update is required to avoid overselling a batch.
    @Lock(PESSIMISTIC_WRITE)
    @Query("""
        SELECT p FROM PricingDetails p
        WHERE p.productDetails.productId = :productId AND p.stockQuantity > :minQty
        ORDER BY p.manufacturingDate ASC
    """)
    List<PricingDetails> lockAvailableBatchesForDebit(
            @Param("productId") String productId, @Param("minQty") Long minQty);

    // Packaging-scoped FIFO lock, used when a debit request targets one specific variant only.
    @Lock(PESSIMISTIC_WRITE)
    @Query("""
        SELECT p FROM PricingDetails p
        WHERE p.productDetails.productId = :productId
          AND p.packagingDetails.packagingId = :packagingId
          AND p.stockQuantity > :minQty
        ORDER BY p.manufacturingDate ASC
    """)
    List<PricingDetails> lockAvailableBatchesForDebitByPackaging(
            @Param("productId") String productId, @Param("packagingId") String packagingId, @Param("minQty") Long minQty);

    @Query("""
        SELECT COALESCE(SUM(p.stockQuantity), 0) FROM PricingDetails p
        WHERE p.productDetails.productId = :productId AND p.packagingDetails.packagingId = :packagingId
    """)
    Long getTotalStockByProductIdAndPackagingId(
            @Param("productId") String productId, @Param("packagingId") String packagingId);

    long countByPackagingDetails_PackagingId(String packagingId);
}

