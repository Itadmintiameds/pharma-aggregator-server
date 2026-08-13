package com.example.pharmaaggregatorserver.repository.buyer;

import com.example.pharmaaggregatorserver.entity.buyer.Buyer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface BuyerRepository extends JpaRepository<Buyer, String> {

    /**
     * Extracts the 4-digit numeric suffix from all existing buyer IDs and
     * returns the maximum value found. Mirrors
     * repository.seller.SellerRepository#findMaxSellerSequence().
     * <p>
     * Buyer ID format: [2-char org name][buyer type abbreviation][4-digit sequence]
     * Returns 0 if no buyers exist yet (so the first ID will be 0001).
     */
    @Query(value = """
            SELECT COALESCE(MAX(CAST(SUBSTRING(buyer_id, LENGTH(buyer_id) - 3, 4) AS INTEGER)), 0)
            FROM tbl_buyer
            """, nativeQuery = true)
    Integer findMaxBuyerSequence();

    /**
     * Acquires a PostgreSQL transaction-scoped advisory lock using key 54321
     * (a distinct key from SellerRepository's 12345) so buyer ID generation
     * never contends with seller ID generation. Ensures only one transaction
     * at a time can generate a buyer ID, preventing duplicate sequence
     * numbers under concurrent admin approvals. Released automatically on
     * commit/rollback; safe for multi-node deployments as the lock resides in
     * PostgreSQL, not the JVM.
     */
    @Query(value = "SELECT pg_advisory_xact_lock(54321)", nativeQuery = true)
    void acquireBuyerIdLock();

    Optional<Buyer> findByUser_BuyerUserId(Long buyerUserId);
}
