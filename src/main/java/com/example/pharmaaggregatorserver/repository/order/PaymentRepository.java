package com.example.pharmaaggregatorserver.repository.order;

import com.example.pharmaaggregatorserver.entity.order.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, String> {

    Optional<Payment> findByProviderTransactionId(String providerTransactionId);

    Optional<Payment> findByPaymentId(String paymentId);

    Optional<Payment> findByOrder_OrderId(String orderId);

    /**
     * Extracts the 5-digit numeric suffix from payment IDs sharing the given
     * date prefix (e.g. "PAY-20260814-") and returns the maximum found.
     * Payment ID format: PAY-{yyyyMMdd}-{5-digit-seq}, sequence resets daily.
     */
    @Query(value = """
            SELECT COALESCE(MAX(CAST(SUBSTRING(payment_id, LENGTH(:prefix) + 1, 5) AS INTEGER)), 0)
            FROM tbl_payment
            WHERE payment_id LIKE CONCAT(:prefix, '%')
            """, nativeQuery = true)
    Integer findMaxPaymentSequenceForPrefix(@Param("prefix") String prefix);

    /**
     * Acquires a PostgreSQL transaction-scoped advisory lock using key 98766
     * — distinct from SellerRepository's 12345, BuyerRepository's 54321, and
     * OrderRepository's 98765 — so payment-ID generation never contends with
     * order/seller/buyer ID generation.
     */
    @Query(value = "SELECT pg_advisory_xact_lock(98766)", nativeQuery = true)
    void acquirePaymentIdLock();
}
