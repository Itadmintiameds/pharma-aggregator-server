package com.example.pharmaaggregatorserver.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_district_master")
@Data
public class DistrictMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "district_id")
    private Integer districtId;

    // Add the ManyToOne relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_id", nullable = false, insertable = false, updatable = false)
    private StateMaster state;

    // Keep this column for direct access
    @Column(name = "state_id", nullable = false)
    private Integer stateId;

    @Column(name = "district_code", unique = true, nullable = false, length = 10)
    private String districtCode;

    @Column(name = "district_name", nullable = false, length = 100)
    private String districtName;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;
}