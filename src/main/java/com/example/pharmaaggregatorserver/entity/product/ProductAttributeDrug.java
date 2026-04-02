package com.example.pharmaaggregatorserver.entity.product;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tm_product_attribute_drug")
public class ProductAttributeDrug {

    @Id
    @Column(name = "product_attribute_id")
    private String productAttributeId;

    @Column(name = "therapeutic_category_id")
    private String therapeuticCategoryId;

    @Column(name = "therapeutic_subcategory_id")
    private String therapeuticSubcategoryId;

    @Column(name = "dosage_form")
    private String dosageForm;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "modified_by")
    private String modifiedBy;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore
    private ProductDetails productDetails;

    @OneToMany(mappedBy = "productAttributeDrug", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<ProductMolecule> productMolecules = new HashSet<>();
}
