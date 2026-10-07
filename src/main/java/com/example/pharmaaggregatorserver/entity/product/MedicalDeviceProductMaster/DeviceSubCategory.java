package com.example.pharmaaggregatorserver.entity.product.MedicalDeviceProductMaster;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_device_sub_category_master")
@Getter
@Setter
public class DeviceSubCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "device_sub_cat_id")
    private Long deviceSubCatId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_cat_id", nullable = false)
    private DeviceCategory deviceCategory;

    @Column(name = "sub_category_name", nullable = false, length = 255)
    private String subCategoryName;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}