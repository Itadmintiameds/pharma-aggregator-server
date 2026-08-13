package com.example.pharmaaggregatorserver.entity.temp.buyer;

import com.example.pharmaaggregatorserver.entity.buyer.BuyerUser;
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
@Table(name = "tbl_temp_buyer")
@Getter
@Setter
public class TempBuyer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "temp_buyer_id")
    private Long tempBuyerId;

    @Column(name = "temp_buyer_request_id", nullable = false, length = 100)
    private String tempBuyerRequestId;

    // The BuyerUser account that started this registration, if the
    // signup-first flow already has a login attached. Nullable — a draft may
    // be started/linked to a login at a later point.
    // @JsonIgnore: BuyerUser carries passwordHash/resetPasswordToken with no
    // field-level exclusion of its own, so this relation must never serialize
    // directly. Callers that need the linked account only ever need its id,
    // which they already hold (it's the same id they passed in as
    // buyerUserId when creating/updating this draft).
    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_user_id", unique = true, nullable = true)
    private BuyerUser user;

    @Column(name = "organization_name", length = 150)
    private String organizationName;

    @Column(name = "org_logo_url")
    private String orgLogoUrl;

    @Column(name = "org_logo_file_name")
    private String orgLogoFileName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_type_id")
    private BuyerTypeMaster buyerType;

    // ---------------- 1:1 ----------------

    @OneToOne(mappedBy = "buyer", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private TempBuyerAddress address;

    @OneToOne(mappedBy = "buyer", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private TempBuyerContact contact;

    // ---------------- 1:N ----------------

    @OneToMany(mappedBy = "buyer", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TempBuyerDocument> documents = new ArrayList<>();

    @OneToMany(mappedBy = "tempBuyer", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TempBuyerReviewHistory> reviewHistories = new ArrayList<>();

    @Column(name = "gst_number", length = 100)
    private String gstNumber;

    @Column(name = "gst_file_url")
    private String gstFileUrl;

    @Column(name = "gst_file_name")
    private String gstFileName;

    @Column(name = "is_gst_verified", columnDefinition = "boolean default false", nullable = false)
    private boolean isGstVerified = false;

    @Column(name = "pan_number", length = 100)
    private String panNumber;

    @Column(name = "pan_file_url")
    private String panFileUrl;

    @Column(name = "pan_file_name")
    private String panFileName;

    @Column(name = "is_pan_verified", columnDefinition = "boolean default false", nullable = false)
    private boolean isPanVerified = false;

    @Column(name = "status", nullable = false, length = 100)
    private String status;

    @Column(name = "terms_accepted", columnDefinition = "boolean default false")
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

    public void addDocument(TempBuyerDocument doc) {
        documents.add(doc);
        doc.setBuyer(this);
    }
}
