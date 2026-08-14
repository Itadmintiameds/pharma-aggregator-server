package com.example.pharmaaggregatorserver.repository.order;

import com.example.pharmaaggregatorserver.entity.order.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findBySellerOrder_SellerOrderId(String sellerOrderId);

    /**
     * Counts existing invoices whose invoiceNumber starts with the given
     * per-seller-per-financial-year prefix (e.g. "INV-TEHOS0001-2526-").
     * Used to derive the next sequence number. Simple counting query per the
     * spec — not advisory-lock-protected, so a genuine concurrent-generation
     * race for the exact same seller+FY could in theory produce a duplicate
     * sequence; acceptable for this build given invoice generation is a
     * low-concurrency, seller-initiated action.
     */
    @Query(value = """
            SELECT COUNT(*) FROM tbl_invoice WHERE invoice_number LIKE CONCAT(:prefix, '%')
            """, nativeQuery = true)
    long countByInvoiceNumberPrefix(@Param("prefix") String prefix);
}
