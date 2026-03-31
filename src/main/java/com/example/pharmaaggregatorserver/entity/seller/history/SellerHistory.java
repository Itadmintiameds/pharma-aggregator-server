package com.example.pharmaaggregatorserver.entity.seller.history;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Immutable audit snapshot of a Seller captured immediately BEFORE each
 * admin-approved update overwrites the live row.
 *
 * Design decisions:
 *  - pendingSellerId is NOT stored — PendingSeller is deleted on approval,
 *    so any reference would immediately be a dangling pointer with no value.
 *  - Master-data IDs (state, district, taluka, companyType, sellerType,
 *    productTypes) are stored alongside their human-readable names frozen at
 *    snapshot time. IDs alone are not safe — master rows can be renamed,
 *    merged, or re-seeded in the future.
 *  - S3 files are NEVER deleted or moved. Old URLs stored here remain valid
 *    inside sellers/{sellerId}/... indefinitely as a file audit trail.
 *  - This row is written once and never updated.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tbl_seller_history", indexes = {
        @Index(name = "idx_seller_history_seller_id", columnList = "seller_id, snapshot_at DESC")
})
public class SellerHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    // ── Approval metadata ─────────────────────────────────────────────────────
    @Column(name = "seller_id", nullable = false, length = 50)
    private String sellerId;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @CreationTimestamp
    @Column(name = "snapshot_at", nullable = false, updatable = false)
    private LocalDateTime snapshotAt;

    // ═══════════════════════════════════════════════════════════════════════
    // Core seller fields
    // ═══════════════════════════════════════════════════════════════════════

    @Column(name = "seller_name", length = 200)
    private String sellerName;

    @Column(name = "seller_image_url", columnDefinition = "TEXT")
    private String sellerImageUrl;

    @Column(name = "phone", length = 100)
    private String phone;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "website", columnDefinition = "TEXT")
    private String website;

    @Column(name = "status", length = 100)
    private String status;

    @Column(name = "terms_accepted")
    private boolean termsAccepted;

    // Company type — ID kept for traceability, name frozen at snapshot time
    @Column(name = "company_type_id")
    private Long companyTypeId;

    @Column(name = "company_type_name", length = 200)
    private String companyTypeName;

    // Seller type — ID kept for traceability, name frozen at snapshot time
    @Column(name = "seller_type_id")
    private Long sellerTypeId;

    @Column(name = "seller_type_name", length = 200)
    private String sellerTypeName;

    // ═══════════════════════════════════════════════════════════════════════
    // Address snapshot
    // ═══════════════════════════════════════════════════════════════════════

    @Column(name = "state_id")
    private Long stateId;

    @Column(name = "state_name", length = 200)
    private String stateName;

    @Column(name = "district_id")
    private Long districtId;

    @Column(name = "district_name", length = 200)
    private String districtName;

    @Column(name = "taluka_id")
    private Long talukaId;

    @Column(name = "taluka_name", length = 200)
    private String talukaName;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "street", columnDefinition = "TEXT")
    private String street;

    @Column(name = "building_no", length = 100)
    private String buildingNo;

    @Column(name = "landmark", columnDefinition = "TEXT")
    private String landmark;

    @Column(name = "pin_code", length = 20)
    private String pinCode;

    // ═══════════════════════════════════════════════════════════════════════
    // Coordinator snapshot
    // ═══════════════════════════════════════════════════════════════════════

    @Column(name = "coordinator_name", length = 100)
    private String coordinatorName;

    @Column(name = "coordinator_designation", length = 100)
    private String coordinatorDesignation;

    @Column(name = "coordinator_email", length = 100)
    private String coordinatorEmail;

    @Column(name = "coordinator_mobile", length = 100)
    private String coordinatorMobile;

    // ═══════════════════════════════════════════════════════════════════════
    // Bank details snapshot
    // ═══════════════════════════════════════════════════════════════════════

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "bank_branch", length = 100)
    private String bankBranch;

    @Column(name = "bank_ifsc_code", length = 100)
    private String bankIfscCode;

    @Column(name = "bank_account_number", length = 100)
    private String bankAccountNumber;

    @Column(name = "bank_account_holder_name", length = 100)
    private String bankAccountHolderName;

    /** Old S3 URL — file stays in sellers/{sellerId}/bankdocument/ forever */
    @Column(name = "bank_document_file_url", columnDefinition = "TEXT")
    private String bankDocumentFileUrl;

    @Column(name = "is_bank_document_verified")
    private boolean isBankDocumentVerified;

    // ═══════════════════════════════════════════════════════════════════════
    // GST snapshot
    // ═══════════════════════════════════════════════════════════════════════

    @Column(name = "gst_number", length = 100)
    private String gstNumber;

    /** Old S3 URL — file stays in sellers/{sellerId}/gst/ forever */
    @Column(name = "gst_file_url", columnDefinition = "TEXT")
    private String gstFileUrl;

    @Column(name = "is_gst_verified")
    private boolean isGstVerified;

    // ═══════════════════════════════════════════════════════════════════════
    // Product types snapshot
    //
    // Both IDs and names are stored and frozen at snapshot time.
    // Example:
    //   productTypeIds   = "1,3,5"
    //   productTypeNames = "Allopathy,Ayurveda,Homeopathy"
    // ═══════════════════════════════════════════════════════════════════════

    @Column(name = "product_type_ids", length = 500)
    private String productTypeIds;

    @Column(name = "product_type_names", length = 1000)
    private String productTypeNames;

    // ═══════════════════════════════════════════════════════════════════════
    // Documents snapshot — stored as a JSON array string.
    //
    // productTypeName is included in each element so the document record
    // is fully self-contained and readable without any table joins.
    //
    // Example:
    // [
    //   {
    //     "sellerDocumentId": 12,
    //     "productTypeId": 3,
    //     "productTypeName": "Ayurveda",
    //     "documentNumber": "DL-MH-2024-001",
    //     "documentFileUrl": "https://s3.../sellers/SELL001/licenses/...",
    //     "licenseIssueDate": "2024-01-01",
    //     "licenseExpiryDate": "2026-01-01",
    //     "licenseIssuingAuthority": "FDA Maharashtra",
    //     "licenseStatus": "Active",
    //     "isDocumentVerified": true
    //   }
    // ]
    // ═══════════════════════════════════════════════════════════════════════

    @Column(name = "documents_json", columnDefinition = "TEXT")
    private String documentsJson;
}