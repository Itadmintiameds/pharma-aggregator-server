package com.example.pharmaaggregatorserver.entity.product.MedicalDeviceProductMaster;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_device_category_master")
@Getter
@Setter
public class DeviceCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "device_cat_id")
    private Long deviceCatId;

    @Column(name = "device_name", nullable = false, length = 255)
    private String deviceName;


    @Column(name = "device_category_type", length = 100)
    private String deviceCategoryType;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Automatically set timestamps
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
