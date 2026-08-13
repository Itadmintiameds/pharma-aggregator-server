package com.example.pharmaaggregatorserver.entity.temp.buyer;

import com.example.pharmaaggregatorserver.entity.master.DocumentTypeMaster;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "tbl_temp_buyer_document")
public class TempBuyerDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "document_id")
    private Long tempBuyerDocumentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buyer_id", nullable = false)
    @JsonIgnore
    private TempBuyer buyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_type_id")
    private DocumentTypeMaster documentType;

    @Column(name = "document_number", nullable = false, length = 100)
    private String documentNumber;

    @Column(name = "document_file_url", nullable = false, columnDefinition = "VARCHAR(255) DEFAULT 'PENDING'")
    private String documentFileUrl = "PENDING";

    @Column(name = "document_file_name")
    private String documentFileName;

    @Column(name = "is_document_verified", columnDefinition = "boolean default false")
    private boolean isDocumentVerified = false;

    @Column(name = "license_issue_date")
    private LocalDate licenseIssueDate;

    @Column(name = "license_expiry_date")
    private LocalDate licenseExpiryDate;

    @Column(name = "license_issuing_authority", length = 255)
    private String licenseIssuingAuthority;

    @Column(name = "license_status", length = 20)
    private String licenseStatus; // Active/Expired - system generated

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_active")
    private Boolean isActive = true;

    /**
     * Calculates and updates license status based on issue and expiry dates.
     * Mirrors entity.temp.seller.TempSellerDocument#updateLicenseStatus().
     */
    public void updateLicenseStatus() {
        if (licenseIssueDate != null && licenseExpiryDate != null) {
            LocalDate currentDate = LocalDate.now();
            if (!currentDate.isBefore(licenseIssueDate) && !currentDate.isAfter(licenseExpiryDate)) {
                this.licenseStatus = "Active";
            } else {
                this.licenseStatus = "Expired";
            }
        } else {
            this.licenseStatus = "Expired";
        }
    }

    @PrePersist
    @PreUpdate
    private void autoUpdateLicenseStatus() {
        updateLicenseStatus();
    }
}
