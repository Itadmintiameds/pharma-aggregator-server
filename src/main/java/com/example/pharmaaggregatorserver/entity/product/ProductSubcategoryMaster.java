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
@Table(name = "tm_product_subcategory_master")
public class ProductSubcategoryMaster {

    @Id
    @Column(name = "product_subcategory_id")
    private Long productSubcategoryId;

    @Column(name = "product_subcategory")
    private String productSubcategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_category_id", nullable = false)
    @JsonIgnore
    private ProductCategoryMaster productCategoryMaster;
}
