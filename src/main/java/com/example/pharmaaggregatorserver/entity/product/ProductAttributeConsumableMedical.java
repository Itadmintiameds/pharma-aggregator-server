package com.example.pharmaaggregatorserver.entity.product;

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
@Table(name = "tm_product_attribute_consumable_medical")
public class ProductAttributeConsumableMedical {

    @Id
    @Column(name = "product_attribute_id")
    private String productAttributeId;

    // FK → tbl_device_category_master
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_cat_id" ,nullable = false)
    @JsonIgnoreProperties
    private DeviceCategory deviceCategory;

    // FK → tbl_device_category_master
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_sub_cat_id" ,nullable = false)
    @JsonIgnoreProperties
    private DeviceSubCategory deviceSubCategory;

    @Column(name = "brand_name")
    private String brandName;

//    // FK → tbl_consumable_material_type_master
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "material_type_id")
//    @JsonIgnoreProperties
//    private ConsumableMaterialType materialType;

    // MANY-TO-MANY with ConsumableMaterialType (changed from @ManyToOne)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "tm_product_consumable_material_mapping",
            joinColumns = @JoinColumn(name = "product_attribute_id"),
            inverseJoinColumns = @JoinColumn(name = "material_type_id")
    )
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "productAttributes"})
    private List<ConsumableMaterialType> materialTypes = new ArrayList<>();

    // FK → tbl_diamension_size_master
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "diamension_id")
//    @JsonIgnoreProperties
//    private DimensionSize dimensionSize;
    @Column(name = "dimension_Size")
    private String dimensionSize;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_specification_unit_id", nullable = true)
    @JsonIgnore
    private DeviceSpecificationUnit deviceSpecificationUnit;

    @Column(name = "sterile_or_non_sterile")
    private String sterileOrNonSterile;

    @Column(name = "disposal_or_reusable")
    private String disposalOrReusable;

    @Column(name = "shelf_life")
    private String shelfLife;

    @Column(name = "purpose")
    private String purpose;

    @Column(name = "key_features_specifications", columnDefinition = "TEXT")
    private String keyFeaturesSpecifications;

    @Column(name = "safety_instructions", columnDefinition = "TEXT")
    private String safetyInstructions;

//    // FK → tbl_certification_master
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "certification_id")
//    @JsonIgnoreProperties
//    private Certification certification;
//
//    @Column(name = "compliance_certificate_url")
//    private String complianceCertificateUrl;

    // Multiple certification types (CDSCO / ISO / CE / BIS)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "tm_consumable_medical_certifications",
            joinColumns = @JoinColumn(name = "product_attribute_id"),
            inverseJoinColumns = @JoinColumn(name = "certification_id")
    )
    @JsonIgnore
    private List<Certification> certifications = new ArrayList<>();

    @OneToMany(mappedBy = "consumableMedical", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductCertificateDocument> certificateDocuments = new ArrayList<>();

    // FK → tbl_certification_master
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_device_id")
    @JsonIgnoreProperties
    private MedicalDeviceType MedicalDeviceType;

//    @Column(name = "certification_document_path")
//    private String certificationDocumentPath;

//    @Column(name = "country_of_origin")
//    private String countryOfOrigin;

    // FK → tbl_device_category_master
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id" ,nullable = false)
    @JsonIgnoreProperties
    private CountryMaster countryMaster;

    @Column(name = "manufacturer_name")
    private String manufacturerName;

//    @Column(name = "storage_condition")
//    private String storageCondition;

    // FK → tbl_storage_condition_master
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storage_condition_id" ,nullable = false)
    @JsonIgnoreProperties
    private StorageConditionMaster storageConditionMaster;

    @Column(name = "brochure_type")
    private String brochureType;

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
