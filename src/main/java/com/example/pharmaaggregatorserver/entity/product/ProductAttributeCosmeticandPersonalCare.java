package com.example.pharmaaggregatorserver.entity.product;

import com.example.pharmaaggregatorserver.entity.product.CosmeticPersonalCareMasters.HairType;
import com.example.pharmaaggregatorserver.entity.product.CosmeticPersonalCareMasters.IntendedUseArea;
import com.example.pharmaaggregatorserver.entity.product.CosmeticPersonalCareMasters.SkinType;
import com.example.pharmaaggregatorserver.entity.product.MedicalDeviceProductMaster.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "tm_product_attribute_cosmetic_and_personal_use")

public class ProductAttributeCosmeticandPersonalCare {
    @Id
    @Column(name = "product_attribute_id")
    private String productAttributeId;

    // FK → Product Type
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_category_id" ,nullable = false)
    @JsonIgnoreProperties
    private ProductCategoryMaster productCategoryMaster;

    // FK → Product Subtype
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_subcategory_id" ,nullable = false)
    @JsonIgnoreProperties
    private ProductSubcategoryMaster productSubcategoryMaster;

    @Column(name = "brand_name")
    private String brandName;

    @Column(name = "variant_name")
    private String VariantName;

    @Column(name = "gender")
    private String Gender;

    // MANY-TO-MANY with cosmetic_and_personal_use_mapping
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "tm_cosmetic_and_personal_use_mapping",
            joinColumns = @JoinColumn(name = "product_attribute_id"),
            inverseJoinColumns = @JoinColumn(name = "use_area_id")
    )
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "productAttributes"})
    private List<IntendedUseArea> IntendedUseArea = new ArrayList<>();

    // MANY-TO-MANY Skin / Hair Type
//    @ManyToMany(fetch = FetchType.LAZY)
//    @JoinTable(
//            name = "tm_cosmetic_skin_hair_type_mapping",
//            joinColumns = @JoinColumn(name = "product_attribute_id"),
//            inverseJoinColumns = @JoinColumn(name = "type_id")
//    )
//    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "productAttributes"})
//    private List<SkinHairType> SkinHairType = new ArrayList<>();

    //skin type
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "tm_cosmetic_skin_type_mapping",
            joinColumns = @JoinColumn(name = "product_attribute_id"),
            inverseJoinColumns = @JoinColumn(name = "type_id")
    )
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "productAttributes"})
    private List<SkinType> SkinType = new ArrayList<>();

    //Hair type
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "tm_cosmetic_hair_type_mapping",
            joinColumns = @JoinColumn(name = "product_attribute_id"),
            inverseJoinColumns = @JoinColumn(name = "type_id")
    )
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "productAttributes"})
    private List<HairType> hairTypes = new ArrayList<>();

    //Active Ingredients
    @Column(name = "active_ingredients")
    private String ActiveIngredients;

    //Net Quantity/ StrengthD
    @Column(name = "net_quantity_strength")
    private String NetQuantityStrength;

    // FK → age_group
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "age_group_id" ,nullable = false)
    @JsonIgnoreProperties
    private AgeGroupMaster ageGroupMaster;

    @Column(name = "product_claims")
    private String ProductClaims;

//    @Column(name = "warnings_precautions")
//    private String WarningsPrecautions;

    // FK → tbl_storage_condition_master
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storage_condition_id" ,nullable = false)
    @JsonIgnoreProperties
    private StorageConditionMaster storageConditionMaster;

    @Column(name = "manufacturer_name")
    private String manufacturerName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id" ,nullable = false)
    @JsonIgnoreProperties
    private CountryMaster countryMaster;

    // Multiple certification types (CDSCO / ISO / CE / BIS)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "tm_cosmetic_and_personal_use_certifications",
            joinColumns = @JoinColumn(name = "product_attribute_id"),
            inverseJoinColumns = @JoinColumn(name = "certification_id")
    )
    @JsonIgnore
    private List<Certification> certifications = new ArrayList<>();

    @OneToMany(mappedBy = "cosmeticAndPersonalUse", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductCertificateDocument> certificateDocuments = new ArrayList<>();

    @Column(name = "brochure_path")
    private String brochurePath;

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
    @JsonIgnoreProperties
    private ProductDetails productDetails;

}
