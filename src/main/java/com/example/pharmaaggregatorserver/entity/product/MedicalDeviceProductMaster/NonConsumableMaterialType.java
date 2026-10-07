package com.example.pharmaaggregatorserver.entity.product.MedicalDeviceProductMaster;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_non_consumable_material_type_master")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NonConsumableMaterialType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "material_type_id")
    private Long materialTypeId;

    @Column(name = "material_type_name", nullable = false)
    private String materialTypeName;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
