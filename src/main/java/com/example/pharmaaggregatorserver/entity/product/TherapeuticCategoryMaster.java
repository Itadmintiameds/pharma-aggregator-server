package com.example.pharmaaggregatorserver.entity.product;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tm_therapeutic_category_master")
public class TherapeuticCategoryMaster {

    @Id
    @Column(name = "therapeutic_category_id")
    private String therapeuticCategoryId;

    @Column(name = "therapeutic_category")
    private String therapeuticCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonIgnore
    private Category category;

    @OneToMany(mappedBy = "therapeuticCategoryMaster", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<TherapeuticSubcategoryMaster> therapeuticSubcategoryMasters;

}
