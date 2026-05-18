package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.PricingDetails;
import com.example.pharmaaggregatorserver.entity.product.ProductDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
    """)
    boolean existsByBatchLotNumberAndUserId(
            @Param("batchLotNumber") String batchLotNumber,
            @Param("userId") Long userId
    );
}

