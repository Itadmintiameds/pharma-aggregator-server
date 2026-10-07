package com.example.pharmaaggregatorserver.entity.product;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
@Table(name = "tm_therapeutic_subcategory_master")
public class TherapeuticSubcategoryMaster {

    @Id
    @Column(name = "therapeutic_subcategory_id")
    private String therapeuticSubcategoryId;

    @Column(name = "therapeutic_subcategory")
    private String therapeuticSubcategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "therapeutic_category_id", nullable = false)
    @JsonIgnore
    private TherapeuticCategoryMaster therapeuticCategoryMaster;
}
