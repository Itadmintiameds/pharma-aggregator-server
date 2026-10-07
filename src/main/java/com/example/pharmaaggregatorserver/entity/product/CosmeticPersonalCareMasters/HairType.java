package com.example.pharmaaggregatorserver.entity.product.CosmeticPersonalCareMasters;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tm_hair_type_master")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class HairType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "type_id")
    private Long typeId;

//    @Column(name = "category", nullable = false, length = 20)
//    @Enumerated(EnumType.STRING)
//    private SkinType.Category category;

    @Column(name = "type_name", nullable = false, unique = true, length = 50)
    private String typeName;

    @Column(name = "type_code", unique = true, length = 30)
    private String typeCode;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "icon_class", length = 100)
    private String iconClass;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

//    public enum Category {
//        SKIN, HAIR
//    }

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
