package com.example.pharmaaggregatorserver.entity.buyer;

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
@Table(name = "tbl_buyer_contact")
public class BuyerContact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contact_id")
    private Long buyerContactId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buyer_id", unique = true, nullable = false)
    @JsonIgnore
    private Buyer buyer;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "designation", length = 100)
    private String designation;

    @Column(name = "email", unique = true, length = 100)
    private String email;

    @Column(name = "isEmailVerified")
    private boolean isEmailVerified;

    @Column(name = "mobile", unique = true, length = 100)
    private String mobile;

    @Column(name = "isPhoneVerified")
    private boolean isPhoneVerified;

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
