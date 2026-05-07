package com.example.pharmaaggregatorserver.entity.product;

import com.example.pharmaaggregatorserver.entity.product.MedicalDeviceProductMaster.Certification;
import com.example.pharmaaggregatorserver.entity.product.MedicalDeviceProductMaster.CountryMaster;
import com.example.pharmaaggregatorserver.entity.product.MedicalDeviceProductMaster.StorageConditionMaster;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tm_product_attribute_food_infant")
public class ProductAttributeFoodInfant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "product_attribute_id")
    private String productAttributeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_category_id", nullable = false)
    private ProductCategoryMaster productCategoryMaster;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_subcategory_id", nullable = false)
    private ProductSubcategoryMaster productSubcategoryMaster;

    @Column(name = "brand_name")
    private String brandName;

    @Column(name = "variant_name")
    private String variantName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_form_id", nullable = false)
    private ProductFormMaster productFormMaster;

    @Column(name = "net_quantity")
    private String netQuantity;

    @Column(name = "serving_size")
    private String servingSize;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "age_group_id", nullable = false)
    private AgeGroupMaster ageGroupMaster;

    @Column(name = "veg_nonveg_indicator")
    private String vegNonvegIndicator;

    @Column(name = "allergen_information")
    private String allergenInformation;

    @Column(name = "nutritional_information")
    private String nutritionalInformation;

    @Column(name = "nutritional_information_image_url")
    private String nutritionalInformationImageUrl;

    @Column(name = "active_ingredients")
    private String activeIngredients;

    @Column(name = "additives_preservatives")
    private String additivesPreservatives;

    @Column(name = "product_claims")
    private String productClaims;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storage_condition_id", nullable = false)
    private StorageConditionMaster storageConditionMaster;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", nullable = false)
    @JsonIgnore
    private CountryMaster countryMaster;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "tm_food_infant_certifications",
            joinColumns = @JoinColumn(name = "product_attribute_id"),
            inverseJoinColumns = @JoinColumn(name = "certification_id")
    )
    @JsonIgnore
    private List<Certification> certifications = new ArrayList<>();

    @OneToMany(mappedBy = "productAttributeFoodInfant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductCertificateDocument> certificateDocuments = new ArrayList<>();

    @Column(name = "product_user_manual")
    private String productUserManual;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore
    private ProductDetails productDetails;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "modified_by")
    private String modifiedBy;

    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;
}
