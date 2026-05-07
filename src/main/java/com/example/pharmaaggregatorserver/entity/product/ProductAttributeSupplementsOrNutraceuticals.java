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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tm_product_attribute_supplements_or_nutraceuticals")
public class ProductAttributeSupplementsOrNutraceuticals {

    @Id
    @Column(name = "product_attribute_id")
    private String productAttributeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "therapeutic_category_id", nullable = false)
    @JsonIgnore
    private TherapeuticCategoryMaster therapeuticCategory;      // Therapeutic Category*

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "therapeutic_sub_category_id", nullable = false)
    @JsonIgnore
    private TherapeuticSubcategoryMaster therapeuticSubCategory;        // Therapeutic Subcategory*

    @Column(name = "brand_name", nullable = false, length = 60)
    private String brandName;       // Brand Name*

    @Column(name = "variant_name", length = 60)
    private String variantName;       // Variant Name

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dosage_form_id", nullable = false)
    @JsonIgnore
    private DosageForm dosageForm;      // Dosage Form*

    @Column(name = "net_quantity", nullable = false, length = 20)
    private String netQuantity;       // Net Quantity*

    @Column(name = "strength", nullable = false)
    private String strength;        // Strength / Composition*

    @Column(name = "active_ingredients", nullable = false)
    private String activeIngredients;       // Active Ingredients*

    @Column(name = "other_ingredients")
    private String otherIngredients;        // Excipients / Other Ingredients

    /*
    Shall provide two dropdowns i.e
    * 1) As per the label. 2) Image upload
    * If selects As per the label, no additional details needs to be captured.
    * If selects, Image upload, single image upload.
     */
    @Column(name = "nutritional_information", nullable = false, length = 60)
    private String nutritionalInformation;      // Nutritional Information Table*

    // If selects, Image upload, single image upload, allowed formats: JPG, PNG, JPEG, SVG and max image upload size: 5MB.
    @Column(name = "nutritional_information_image_url")
    private String nutritionalInformationImageUrl;

    @Column(name = "intended_use", nullable = false)
    private String intendedUse;     // Intended Use / Health Benefit*

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "age_group_master_id", nullable = false)
    @JsonIgnore
    private AgeGroupMaster ageGroupMaster;      // Age Group*

    @Column(name = "gender", nullable = false, length = 60)
    private String gender;      // Gender*

    @Column(name = "veg_or_non_veg_indicator", nullable = false, length = 60)
    private String vegOrNonVegIndicator;        // Veg / Non-Veg Indicator*

    @Column(name = "allergenInformation", nullable = false)
    private String allergenInformation;     // Allergen Information*

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Flavour", nullable = false)
    @JsonIgnore
    private Flavour flavour;        // Flavour*

    @Column(name = "product_claims", nullable = false)
    private String productClaims;       // Product Claims*

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storage_condition_id", nullable = false)
    @JsonIgnore
    private StorageConditionMaster storageConditionMaster;      // Storage Condition*

//    @Column(name = "manufacturer_name", nullable = false, length = 100)
//    private String manufacturerName;        // Manufacturer Name*

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", nullable = false)
    @JsonIgnore
    private CountryMaster countryMaster;        // Country of Origin*

    // Multiple certification types (FSSAI License Number / GMP Certification / ISO Certification / etc.)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "tm_supplements_or_nutraceuticals_certifications",
            joinColumns = @JoinColumn(name = "product_attribute_id"),
            inverseJoinColumns = @JoinColumn(name = "certification_id")
    )
    @JsonIgnore
    private List<Certification> certifications = new ArrayList<>();

    @OneToMany(mappedBy = "supplementsOrNutraceuticals", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductCertificateDocument> certificateDocuments = new ArrayList<>();

    @Column(name = "brochure_path")
    private String brochurePath;        // Product Brochure / User Manual

    @Column(name = "is_active")
    private Boolean isActive = true;

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

}
