package com.example.pharmaaggregatorserver.repository.seller;

import com.example.pharmaaggregatorserver.entity.seller.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
}
