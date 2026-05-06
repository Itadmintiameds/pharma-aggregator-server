package com.example.pharmaaggregatorserver.entity.product.CosmeticPersonalCareMasters;

import com.example.pharmaaggregatorserver.entity.product.ProductAttributeConsumableMedical;
import com.example.pharmaaggregatorserver.entity.product.ProductAttributeCosmeticandPersonalCare;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tm_intended_use_area_master")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntendedUseArea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "use_area_id")
    private Long useAreaId;

    @Column(name = "area_name", nullable = false, unique = true, length = 50)
    private String areaName;

    @Column(name = "area_code", unique = true, length = 20)
    private String areaCode;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "icon_class", length = 100)
    private String iconClass;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Bi-directional mapping (optional but recommended)
    @ManyToMany(mappedBy = "IntendedUseArea")
    @ToString.Exclude
    private java.util.List<ProductAttributeCosmeticandPersonalCare> productAttributes;
}