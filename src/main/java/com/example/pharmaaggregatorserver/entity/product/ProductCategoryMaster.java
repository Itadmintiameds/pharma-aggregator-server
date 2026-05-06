package com.example.pharmaaggregatorserver.entity.product;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "tm_product_category_master")
public class ProductCategoryMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_category_id")
    private Long productCategoryId;

    @Column(name = "product_category")
    private String productCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonIgnore
    private Category category;

    @OneToMany(mappedBy = "productCategoryMaster", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<ProductSubcategoryMaster> productSubcategoryMasters;

//    @OneToMany(mappedBy = "productCategoryMaster", cascade = CascadeType.ALL, orphanRemoval = true)
//    @JsonIgnore
//    private Set<ProductAttributeFoodInfant> productAttributeFoodInfants;
}
