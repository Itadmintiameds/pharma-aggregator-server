package com.example.pharmaaggregatorserver.entity.master;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_company_type_master")
@Getter
@Setter
public class CompanyTypeMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_type_id")
    private Integer companyTypeId;

    @Column(name = "company_type_name", nullable = false, unique = true, length = 100)
    private String companyTypeName;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}