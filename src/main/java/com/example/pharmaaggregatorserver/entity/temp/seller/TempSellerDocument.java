package com.example.pharmaaggregatorserver.entity.temp.seller;


import com.example.pharmaaggregatorserver.entity.master.DocumentTypeMaster;
import com.example.pharmaaggregatorserver.entity.master.ProductTypeMaster;
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
@Table(name = "tbl_temp_seller_document")
public class TempSellerDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "documents_id")
    private Long DocumentsId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    @JsonIgnore
    private TempSeller seller;

    // product_type_id keeps its original NOT NULL constraint in the DB — every
    // row (including seller-level agreements) gets a real ProductTypeMaster,
    // falling back to a reserved placeholder row for agreements/certificates
    // that aren't tied to a real product category. See
    // TempSellerServiceImpl.resolvePlaceholderProductType().
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_type_id", nullable = false)
    private ProductTypeMaster productTypes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_type_id")
    private DocumentTypeMaster documentType;

    @Column(name = "document_number", nullable = false, length = 100)
    private String documentNumber;

    @Column(name = "document_file_url", nullable = false)
    private String documentFileUrl;

    @Column(name = "document_file_name")
    private String documentFileName;

    @Column(name = "is_document_verified", columnDefinition = "boolean default false")
    private boolean isDocumentVerified = false;

    // New license fields
    @Column(name = "license_issue_date")
    private LocalDate licenseIssueDate;

    @Column(name = "license_expiry_date")
    private LocalDate licenseExpiryDate;

    @Column(name = "license_issuing_authority", length = 255)
    private String licenseIssuingAuthority;

    @Column(name = "license_status", length = 20)
    private String licenseStatus; // Active/Expired - This will be system generated

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
     * Method to calculate and update license status based on issue and expiry dates
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
            this.licenseStatus = "Expired"; // Default to Expired if dates are missing
        }
    }

    @PrePersist
    @PreUpdate
    private void autoUpdateLicenseStatus() {
        updateLicenseStatus();
    }

}