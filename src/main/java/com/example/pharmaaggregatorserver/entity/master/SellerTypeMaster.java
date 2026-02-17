package com.example.pharmaaggregatorserver.entity.master;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_seller_type_master")
@Getter
@Setter
public class SellerTypeMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seller_type_id")
    private Integer sellerTypeId;

    @Column(name = "seller_type_name", nullable = false, unique = true, length = 100)
    private String sellerTypeName;

    @Column(name = "is_active")
    private Boolean isActive = true;

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