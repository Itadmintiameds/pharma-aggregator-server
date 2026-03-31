package com.example.pharmaaggregatorserver.entity.seller.profile;

import com.example.pharmaaggregatorserver.entity.master.CompanyTypeMaster;
import com.example.pharmaaggregatorserver.entity.master.ProductTypeMaster;
import com.example.pharmaaggregatorserver.entity.master.SellerTypeMaster;
import com.example.pharmaaggregatorserver.entity.master.StateMaster;
import com.example.pharmaaggregatorserver.entity.master.DistrictMaster;
import com.example.pharmaaggregatorserver.entity.master.TalukaMaster;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tbl_pending_seller")
public class PendingSeller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pending_seller_id")
    private Long pendingSellerId;

    @Column(name = "seller_id")
    private String sellerId; // Original seller ID if updating existing

    @Column(name = "seller_name")
    private String sellerName;

    // Address fields - stored directly in pending_seller table
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_id")
    private StateMaster state;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id")
    private DistrictMaster district;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "taluka_id")
    private TalukaMaster taluka;

    @Column(name = "city")
    private String city;

    @Column(name = "street")
    private String street;

    @Column(name = "building_no")
    private String buildingNo;

    @Column(name = "landmark")
    private String landmark;

    @Column(name = "pin_code")
    private String pinCode;

    // Coordinator fields - stored directly
    @Column(name = "coordinator_name")
    private String coordinatorName;

    @Column(name = "coordinator_designation")
    private String coordinatorDesignation;

    @Column(name = "coordinator_email")
    private String coordinatorEmail;

    @Column(name = "coordinator_mobile")
    private String coordinatorMobile;

    // Bank details fields - stored directly
    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "bank_branch")
    private String bankBranch;

    @Column(name = "bank_ifsc_code")
    private String bankIfscCode;

    @Column(name = "bank_account_number")
    private String bankAccountNumber;

    @Column(name = "bank_account_holder_name")
    private String bankAccountHolderName;

    @Column(name = "bank_document_file_url")
    private String bankDocumentFileUrl;

    // GST fields - stored directly
    @Column(name = "gst_number")
    private String gstNumber;

    @Column(name = "gst_file_url")
    private String gstFileUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_type_id")
    private CompanyTypeMaster companyType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_type_id")
    private SellerTypeMaster sellerType;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "website")
    private String website;

    @Column(name = "terms_accepted")
    private boolean termsAccepted;

    @Column(name = "company_registration_certificate_url")
    private String companyRegistrationCertificateUrl;

    @Column(name = "request_type", nullable = false)
    private String requestType; // "CREATE" or "UPDATE"

    @Column(name = "status", nullable = false)
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED

    @Column(name = "requested_by")
    private String requestedBy;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    // Documents stored as JSON or in separate table with foreign key to pending_seller
    @OneToMany(mappedBy = "pendingSeller", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PendingSellerDocument> documents = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "tbl_pending_seller_product_types",
            joinColumns = @JoinColumn(name = "pending_seller_id"),
            inverseJoinColumns = @JoinColumn(name = "product_type_id")
    )
    private List<ProductTypeMaster> productTypes = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_active")
    private Boolean isActive = true;
}