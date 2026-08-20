package com.example.pharmaaggregatorserver.entity.quote;

import com.example.pharmaaggregatorserver.entity.buyer.BuyerUser;
import com.example.pharmaaggregatorserver.entity.product.ProductDetails;
import com.example.pharmaaggregatorserver.entity.seller.Seller;
import com.example.pharmaaggregatorserver.enums.QuoteRequestStatus;
import com.example.pharmaaggregatorserver.enums.QuoteRequestType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// Backs both "Request Price Option" and "Get a Quote" (RFQ) — one buyer,
// one seller (the product's own seller), distinguished by requestType.
// PRICE_REQUEST-only fields (targetPrice, pincode) and RFQ-only fields
// (deliveryLocation, paymentTerms, companyName, gstNumber, contactPerson)
// stay null on rows of the other type.
@Entity
@Getter
@Setter
@Table(name = "tbl_quote_request")
public class QuoteRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quote_request_id")
    private Long quoteRequestId;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false, length = 20)
    private QuoteRequestType requestType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductDetails product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_user_id", nullable = false)
    @JsonIgnore
    private BuyerUser buyerUser;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "target_price", precision = 12, scale = 2)
    private BigDecimal targetPrice;

    @Column(name = "pincode", length = 6)
    private String pincode;

    @Column(name = "delivery_location")
    private String deliveryLocation;

    @Column(name = "expected_delivery_date")
    private LocalDate expectedDeliveryDate;

    @Column(name = "payment_terms", length = 50)
    private String paymentTerms;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "gst_number", length = 20)
    private String gstNumber;

    @Column(name = "contact_person")
    private String contactPerson;

    @Column(name = "phone", length = 15)
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private QuoteRequestStatus status = QuoteRequestStatus.PENDING;

    @Column(name = "quoted_price", precision = 12, scale = 2)
    private BigDecimal quotedPrice;

    @Column(name = "quote_valid_until")
    private LocalDate quoteValidUntil;

    @Column(name = "seller_notes", columnDefinition = "TEXT")
    private String sellerNotes;

    // Set once this ACCEPTED quote has been converted into an Order (see
    // OrderPlacementServiceImpl) — not a FK on purpose, since Order lives in
    // a different aggregate and this is purely a traceability pointer.
    @Column(name = "order_id")
    private String orderId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
