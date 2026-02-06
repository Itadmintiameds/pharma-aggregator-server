package com.example.pharmaaggregatorserver.entity.temp;

import com.example.pharmaaggregatorserver.entity.CompanyTypeMaster;
import com.example.pharmaaggregatorserver.entity.ProductTypeMaster;
import com.example.pharmaaggregatorserver.entity.SellerTypeMaster;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "temp_seller")
@Getter
@Setter
public class TempSeller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "temp_seller_id")
    private Long tempSellerId;

    @Column(name = "temp_seller_request_id", nullable = false, length = 100)
    private String tempSellerRequestId;

    @Column(name = "seller_name", nullable = false, length = 100)
    private String sellerName;

    // ---------------- 1:1 ----------------

    @OneToOne(mappedBy = "seller", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private TempSellerAddress address;

    @OneToOne(mappedBy = "seller", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private TempSellerCoordinator coordinator;

    @OneToOne(mappedBy = "seller", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private TempSellerBankDetails bankDetails;

    // ---------------- 1:N ----------------

    @OneToMany(mappedBy = "seller", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TempSellerLicence> licences = new ArrayList<>();

    @OneToMany(mappedBy = "seller", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TempSellerDocument> documents = new ArrayList<>();

    // ---------------- N:N ----------------

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "tbl_temp_seller_product_types",
            joinColumns = @JoinColumn(name = "temp_seller_id"),
            inverseJoinColumns = @JoinColumn(name = "product_type_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"temp_seller_id", "product_type_id"})
    )
    private List<ProductTypeMaster> productTypes = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_type_id", nullable = false)
    private CompanyTypeMaster companyType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_type_id", nullable = false)
    private SellerTypeMaster sellerType;

    //    TODO: confirmation required, unique phone is required or not
    @Column(name = "phone", unique = true, nullable = false, length = 100)
    private String phone;

    @Column(name = "isPhoneVerified", nullable = false)
    private boolean isPhoneVerified;

    //    TODO: confirmation required, unique email is required or not
    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "isEmailVerified", nullable = false)
    private boolean isEmailVerified;

    @Column(name = "website")
    private String website;

    @Column(name = "comments")
    private String comments;

    @Column(name = "status", nullable = false, length = 100)
    private String status;

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

    // -------- Helper Methods --------

    public void addLicence(TempSellerLicence licence) {
        licences.add(licence);
        licence.setSeller(this);
    }

    public void addDocument(TempSellerDocument doc) {
        documents.add(doc);
        doc.setSeller(this);
    }

}
