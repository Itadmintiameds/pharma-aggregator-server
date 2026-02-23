package com.example.pharmaaggregatorserver.entity.temp.seller;

import com.example.pharmaaggregatorserver.entity.master.CompanyTypeMaster;
import com.example.pharmaaggregatorserver.entity.master.ProductTypeMaster;
import com.example.pharmaaggregatorserver.entity.master.SellerTypeMaster;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tbl_temp_seller")
@Getter
@Setter
public class TempSeller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seller_id")
    private Long tempSellerId;

    @Column(name = "seller_request_id", nullable = false, length = 100)
    private String tempSellerRequestId;

    @Column(name = "seller_name", nullable = false, length = 100)
    private String sellerName;

//    @Column(name = "terms_accepted", nullable = false)
//    private boolean termsAccepted;

    // ---------------- 1:1 ----------------

    @OneToOne(mappedBy = "seller", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private TempSellerAddress address;

    @OneToOne(mappedBy = "seller", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private TempSellerCoordinator coordinator;

    @OneToOne(mappedBy = "seller", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private TempSellerBankDetails bankDetails;

    // ---------------- 1:N ----------------
    @OneToMany(mappedBy = "seller", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TempSellerDocument> documents = new ArrayList<>();

    @Column(name = "gst_number", nullable = false, length = 100)
    private String gstNumber;

    @Column(name = "gst_document_file_url", nullable = false)
    private String gstFileUrl;

    @Column(name = "is_gst_verified", columnDefinition = "boolean default false", nullable = false)
    private boolean isGstVerified = false;

    // ---------------- N:N ----------------

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "tbl_temp_seller_product_types",
            joinColumns = @JoinColumn(name = "temp_seller_id"),
            inverseJoinColumns = @JoinColumn(name = "product_type_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"temp_seller_id", "product_type_id"})
    )
    private List<ProductTypeMaster> productTypes = new ArrayList<>();

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "product_type_id", nullable = false)
//    private ProductTypeMaster productTypes;

    @OneToMany(mappedBy = "tempSeller", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TempSellerReviewHistory> reviewHistories = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_type_id", nullable = false)
    private CompanyTypeMaster companyType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_type_id", nullable = false)
    private SellerTypeMaster sellerType;

    //    TODO: confirmation required, unique phone is required or not
    @Column(name = "phone",  nullable = false, length = 100)
    private String phone;

    @Column(name = "is_phone_verified", nullable = false)
    private boolean isPhoneVerified;

    //    TODO: confirmation required, unique email is required or not
    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "is_email_verified", nullable = false)
    private boolean isEmailVerified;

    @Column(name = "website")
    private String website;

    @Column(name = "status", nullable = false, length = 100)
    private String status; // open, In Progress, Closed

    @Column(name = "terms_accepted", nullable = false, columnDefinition = "boolean default false")
    private boolean termsAccepted = false;

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

    // -------- Helper Methods --------


    public void addDocument(TempSellerDocument doc) {
        documents.add(doc);
        doc.setSeller(this);
    }

}
