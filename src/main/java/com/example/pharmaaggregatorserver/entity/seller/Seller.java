package com.example.pharmaaggregatorserver.entity.seller;

import com.example.pharmaaggregatorserver.entity.auth.User;
import com.example.pharmaaggregatorserver.entity.master.CompanyTypeMaster;
import com.example.pharmaaggregatorserver.entity.master.ProductTypeMaster;
import com.example.pharmaaggregatorserver.entity.master.SellerTypeMaster;
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
@Table(name = "tbl_seller")
public class Seller {

    @Id
    @Column(name = "seller_id")
    private String sellerId;

    @Column(name = "seller_name")
    private String sellerName;

    @OneToOne(mappedBy = "seller", cascade = CascadeType.ALL, orphanRemoval = true)
    private SellerAddress address;

    @OneToOne(mappedBy = "seller", cascade = CascadeType.ALL, orphanRemoval = true)
    private SellerCoordinator coordinator;

    @OneToOne(mappedBy = "seller", cascade = CascadeType.ALL, orphanRemoval = true)
    private SellerBankDetails bankDetails;

    @OneToOne(mappedBy = "seller", cascade = CascadeType.ALL, orphanRemoval = true)
    private SellerGST sellerGST;

    @OneToMany(mappedBy = "seller", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SellerDocument> documents = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "tbl_seller_product_types",
            joinColumns = @JoinColumn(name = "seller_id"),
            inverseJoinColumns = @JoinColumn(name = "product_type_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"seller_id", "product_type_id"})
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

    @Column(name = "is_phone_verified", nullable = false)
    private boolean isPhoneVerified;

    //    TODO: confirmation required, unique email is required or not
    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "is_email_verified", nullable = false)
    private boolean isEmailVerified;

    @Column(name = "website")
    private String website;

    @Column(name = "status", nullable = false, length = 100)
    private String status; // Approved, Correction_required, Rejected

    @Column(name = "terms_accepted", nullable = false, columnDefinition = "boolean default false")
    private boolean termsAccepted = false;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

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

}
