package com.example.pharmaaggregatorserver.entity.product;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pm_product_details_drug")
public class ProductDetailsDrug {

    @Id
    @Column(name = "product_id")
    private String productId;

    @Column(name = "product_category_id")
    private String productCategoryId;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "therapeutic_category")
    private String therapeuticCategory;

    @Column(name = "therapeutic_subcategory")
    private String therapeuticSubcategory;

//    @ManyToMany(
//            fetch = FetchType.LAZY,
//            cascade = { CascadeType.PERSIST, CascadeType.MERGE }
//    )
//    @JoinTable(
//            name = "pm_product_molecule",
//            joinColumns = @JoinColumn(name = "product_id"),
//            inverseJoinColumns = @JoinColumn(name = "molecule_id")
//    )
//    private Set<Molecule> molecules;


    @Column(name = "dosage_form")
    private String dosageForm;

    @Column(name = "strength")
    private Long strength;

    @Column(name = "warnings_precautions")
    private String warningsPrecautions;

    @Column(name = "product_description")
    private String productDescription;

    @Column(name = "product_image")
    private String productImage;

    @Column(name = "product_marketing_url")
    private String productMarketingUrl;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "modified_by")
    private Long modifiedBy;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private PackagingDetailsDrug packagingDetails;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PricingDetailsDrug> pricingDetails;

}
