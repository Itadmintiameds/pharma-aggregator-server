package com.example.pharmaaggregatorserver.entity.buyer;

import com.example.pharmaaggregatorserver.entity.master.BuyerTypeMaster;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "tbl_buyer")
public class Buyer {

    @Id
    @Column(name = "buyer_id")
    private String buyerId;

    // The TempBuyer row this buyer was approved from — kept purely as an
    // audit trail back to the original registration draft. Mirrors
    // entity.seller.Seller#tempSellerId.
    @Column(name = "temp_buyer_id")
    private Long tempBuyerId;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "organization_name", length = 150)
    private String organizationName;

    @Column(name = "org_logo_url")
    private String orgLogoUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_type_id")
    private BuyerTypeMaster buyerType;

    @OneToOne(mappedBy = "buyer", cascade = CascadeType.ALL, orphanRemoval = true)
    private BuyerAddress address;

    @OneToOne(mappedBy = "buyer", cascade = CascadeType.ALL, orphanRemoval = true)
    private BuyerContact contact;

    @OneToMany(mappedBy = "buyer", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BuyerDocument> documents = new ArrayList<>();

    @OneToMany(mappedBy = "buyer", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BuyerDeliveryAddress> deliveryAddresses = new ArrayList<>();

    @Column(name = "gst_number", length = 100)
    private String gstNumber;

    @Column(name = "gst_file_url")
    private String gstFileUrl;

    @Column(name = "is_gst_verified", columnDefinition = "boolean default false", nullable = false)
    private boolean isGstVerified = false;

    @Column(name = "pan_number", length = 100)
    private String panNumber;

    @Column(name = "pan_file_url")
    private String panFileUrl;

    @Column(name = "is_pan_verified", columnDefinition = "boolean default false", nullable = false)
    private boolean isPanVerified = false;

    @Column(name = "status", nullable = false, length = 100)
    private String status; // APPROVED initially

    @Column(name = "terms_accepted", columnDefinition = "boolean default false")
    private boolean termsAccepted = false;

    // @JsonIgnore: same reasoning as TempBuyer.user — BuyerUser exposes
    // passwordHash/resetPasswordToken with no field-level exclusion, so this
    // relation must never serialize directly.
    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_user_id", nullable = false, unique = true)
    private BuyerUser user;

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
