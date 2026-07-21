package com.example.pharmaaggregatorserver.entity.temp.seller;

import com.example.pharmaaggregatorserver.entity.master.DistrictMaster;
import com.example.pharmaaggregatorserver.entity.master.StateMaster;
import com.example.pharmaaggregatorserver.entity.master.TalukaMaster;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "tbl_temp_seller_bank_details")
public class TempSellerBankDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seller_bank_id")
    private Long tempSellerBankDetailsId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", unique = true, nullable = false)
    @JsonIgnore
    private TempSeller seller;

    @Column(name = "bank_name", nullable = false, length = 100)
    private String bankName;

    @Column(name = "branch", nullable = false, length = 100)
    private String branch;

    @Column(name = "ifsc_code", nullable = false, length = 100)
    private String ifscCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_id")
    private StateMaster state;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id")
    private DistrictMaster district;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "taluka_id")
    private TalukaMaster taluka;

    @Column(name = "account_number", nullable = false, length = 100)
    private String accountNumber;

    @Column(name = "account_holder_name", nullable = false, length = 100)
    private String accountHolderName;

    @Column(name = "bank_document_file_url", nullable = false)
    private String bankDocumentFileUrl;

    @Column(name = "is_bank_document_verified", columnDefinition = "boolean default false")
    private boolean isBankDocumentVerified = false;

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
