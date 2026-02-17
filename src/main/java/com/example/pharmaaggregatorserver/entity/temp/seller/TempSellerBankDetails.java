package com.example.pharmaaggregatorserver.entity.temp.seller;

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
    @Column(name = "temp_seller_bank_details_id")
    private Long tempSellerBankDetailsId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "temp_seller_id", unique = true, nullable = false)
    @JsonIgnore
    private TempSeller seller;

    @Column(name = "bank_name", nullable = false, length = 100)
    private String bankName;

    @Column(name = "branch", nullable = false, length = 100)
    private String branch;

    @Column(name = "ifsc_code", nullable = false, length = 100)
    private String ifscCode;

    @Column(name = "account_number", nullable = false, length = 100)
    private String accountNumber;

    @Column(name = "account_holder_name", nullable = false, length = 100)
    private String accountHolderName;

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
}
