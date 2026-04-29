package com.example.pharmaaggregatorserver.entity.product.CosmeticPersonalCareMasters;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tm_cosmetic_pack_type_master")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CosmeticPackType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pack_type_id")
    private Long packTypeId;

    @Column(name = "pack_type_name", nullable = false, unique = true, length = 50)
    private String packTypeName;

    @Column(name = "pack_type_code", unique = true, length = 20)
    private String packTypeCode;

    @Column(name = "category", length = 50)
    @Enumerated(EnumType.STRING)
    private PackCategory category;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "icon_class", length = 100)
    private String iconClass;

    @Column(name = "is_recyclable")
    private Boolean isRecyclable = false;

    @Column(name = "is_refillable")
    private Boolean isRefillable = false;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum PackCategory {
        BOTTLE,      // Bottle, Bottle (Pump), Bottle (Spray)
        TUBE,        // Tube
        CONTAINER,   // Jar, Container
        BOX,         // Box, Cartridge
        POUCH,       // Pouch, Sachet, Pack
        UNIT,        // Piece (Unit)
        SET,         // Kit, Set
        APPLICATOR,  // Roll-On, Stick
        STRIP,       // Strip
        CAN          // Can
    }

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
