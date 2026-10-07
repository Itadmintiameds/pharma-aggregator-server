package com.example.pharmaaggregatorserver.entity.product;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tbl_net_quantity_unit_master")
public class NetQuantityUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "unit_id")
    private Long unitId;

    @Column(name = "unit_name", nullable = false, unique = true, length = 50)
    private String unitName;          // e.g. ml, g, mg, L, kg, tablets, capsules

    @Column(name = "unit_symbol", nullable = false, unique = true, length = 20)
    private String unitSymbol;        // e.g. ml, g, mg, L, kg, tab, cap

    @Column(name = "unit_type", nullable = false, length = 50)
    private String unitType;          // e.g. VOLUME, WEIGHT, COUNT

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = true)
    @JsonIgnore
    private Category category;
}