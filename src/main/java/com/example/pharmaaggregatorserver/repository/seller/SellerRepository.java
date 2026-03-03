package com.example.pharmaaggregatorserver.repository.seller;

import com.example.pharmaaggregatorserver.entity.seller.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SellerRepository extends JpaRepository<Seller, String> {

    /**
     * Extracts the 4-digit numeric suffix from all existing seller IDs
     * and returns the maximum value found.
     *
     * Seller ID format: [2-char name][3-char type][4-digit sequence]
     * e.g. CIMFG0001, SNDST0002
     *
     * Returns 0 if no sellers exist yet (so the first ID will be 0001).
     */
    @Query(value = """
            SELECT COALESCE(MAX(CAST(SUBSTRING(seller_id, LENGTH(seller_id) - 3, 4) AS INTEGER)), 0)
            FROM tbl_seller
            """, nativeQuery = true)
    Integer findMaxSellerSequence();

    Optional<Seller> findByEmail(String email);

    /**
     * Acquires a PostgreSQL transaction-scoped advisory lock using key 12345.
     * This ensures only one transaction at a time can generate a seller ID,
     * preventing duplicate sequence numbers under concurrent admin approvals.
     * The lock is automatically released when the transaction commits or rolls back.
     * Safe for multi-node deployments as the lock resides in PostgreSQL, not the JVM.
     */
    @Query(value = "SELECT pg_advisory_xact_lock(12345)", nativeQuery = true)
    void acquireSellerIdLock();

    Optional<Seller> findByUser_Username(String username);

    //Get All Active Seller
    @Query("SELECT DISTINCT s FROM Seller s " +
            "LEFT JOIN FETCH s.address " +
            "LEFT JOIN FETCH s.coordinator " +
            "LEFT JOIN FETCH s.bankDetails " +
            "LEFT JOIN FETCH s.sellerGST " +
            "LEFT JOIN FETCH s.companyType " +
            "LEFT JOIN FETCH s.sellerType " +
            "LEFT JOIN FETCH s.user " +
            "WHERE s.isActive = true")
    List<Seller> findAllActiveSellers();
}
