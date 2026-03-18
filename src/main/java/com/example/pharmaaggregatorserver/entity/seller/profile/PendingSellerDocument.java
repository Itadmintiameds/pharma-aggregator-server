package com.example.pharmaaggregatorserver.entity.seller.profile;

import com.example.pharmaaggregatorserver.entity.master.ProductTypeMaster;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tbl_pending_seller_document")
public class PendingSellerDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pending_seller_document_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pending_seller_id", nullable = false)
    private PendingSeller pendingSeller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_type_id", nullable = false)
    private ProductTypeMaster productType;

    @Column(name = "document_number", nullable = false)
    private String documentNumber;

    @Column(name = "document_file_url", nullable = false)
    private String documentFileUrl;

    @Column(name = "license_issue_date")
    private LocalDate licenseIssueDate;

    @Column(name = "license_expiry_date")
    private LocalDate licenseExpiryDate;

    @Column(name = "license_issuing_authority")
    private String licenseIssuingAuthority;

    @Column(name = "license_status", length = 20)
    private String licenseStatus; // Active/Expired - This will be system generated

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}