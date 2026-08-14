package com.example.pharmaaggregatorserver.repository.order;

import com.example.pharmaaggregatorserver.entity.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, String> {

    List<Order> findByBuyer_BuyerId(String buyerId);

    Optional<Order> findByOrderId(String orderId);

    List<Order> findByStatus(String status);

    /**
     * Extracts the 5-digit numeric suffix from order IDs sharing the given
     * date prefix (e.g. "ORD-20260814-") and returns the maximum found.
     * Order ID format: ORD-{yyyyMMdd}-{5-digit-seq}, sequence resets daily.
     * Returns 0 if no orders exist yet for that day (so the first ID for the
     * day will end in 00001).
     */
    @Query(value = """
            SELECT COALESCE(MAX(CAST(SUBSTRING(order_id, LENGTH(:prefix) + 1, 5) AS INTEGER)), 0)
            FROM tbl_order
            WHERE order_id LIKE CONCAT(:prefix, '%')
            """, nativeQuery = true)
    Integer findMaxOrderSequenceForPrefix(@Param("prefix") String prefix);

    /**
     * Acquires a PostgreSQL transaction-scoped advisory lock using key 98765
     * — a new key, distinct from SellerRepository's 12345 and
     * BuyerRepository's 54321 — so order-ID generation never contends with
     * seller/buyer ID generation. Ensures only one transaction at a time can
     * generate an order ID for a given day, preventing duplicate sequence
     * numbers under concurrent placements. Released automatically on
     * commit/rollback.
     */
    @Query(value = "SELECT pg_advisory_xact_lock(98765)", nativeQuery = true)
    void acquireOrderIdLock();
}
