package com.example.pharmaaggregatorserver.entity.product;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tm_strength_unit")
public class StrengthUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "strength_unit_id")
    private Long strengthUnitId;

    @Column(name = "unit_name", nullable = false)
    private String unitName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonIgnore
    private Category category;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
