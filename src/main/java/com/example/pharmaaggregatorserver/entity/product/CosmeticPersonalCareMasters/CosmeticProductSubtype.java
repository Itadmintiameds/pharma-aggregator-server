package com.example.pharmaaggregatorserver.entity.product.CosmeticPersonalCareMasters;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tm_cosmetic_product_subtype_master")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CosmeticProductSubtype {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_subtype_id")
    private Long productSubtypeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_type_id", nullable = false, foreignKey = @ForeignKey(name = "fk_subtype_product_type"))
    private CosmeticProductType productType;

    @Column(name = "subtype_name", nullable = false, length = 150)
    private String subtypeName;

    @Column(name = "subtype_code", unique = true, length = 50)
    private String subtypeCode;

    @Column(name = "description", length = 500)
    private String description;

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
}