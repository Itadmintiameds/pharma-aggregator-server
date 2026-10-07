package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.StockLedger;
import com.example.pharmaaggregatorserver.enums.StockTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StockLedgerRepository extends JpaRepository<StockLedger, Long> {

    @Query("""
        select coalesce(sum(l.quantity), 0)
        from StockLedger l
        where l.productDetails.productId = :productId
        and l.transactionType = :type
    """)
    Long sumQuantityByProductIdAndType(
            @Param("productId") String productId,
            @Param("type") StockTransactionType type
    );

    List<StockLedger> findByReferenceIdOrderByCreatedDateAsc(String referenceId);

    List<StockLedger> findByPricingDetails_PricingIdOrderByCreatedDateAsc(String pricingId);
}
