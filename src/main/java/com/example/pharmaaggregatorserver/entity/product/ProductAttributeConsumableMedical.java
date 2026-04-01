package com.example.pharmaaggregatorserver.entity.product;

import com.example.pharmaaggregatorserver.entity.product.MedicalDeviceProductMaster.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tm_product_attribute_consumable_medical")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductAttributeConsumableMedical {

    @Id
    @Column(name = "product_attribute_id")
    private String productAttributeId;

    // FK → tbl_device_category_master
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_cat_id" ,nullable = false)
    @JsonIgnoreProperties
    private DeviceCategory deviceCategory;

    @Column(name = "brand_name")
    private String brandName;

    // FK → tbl_consumable_material_type_master
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_type_id")
    @JsonIgnoreProperties
    private ConsumableMaterialType materialType;

    // FK → tbl_diamension_size_master
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diamension_id")
    @JsonIgnoreProperties
    private DimensionSize dimensionSize;

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



    // FK → tbl_certification_master
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_id")
    @JsonIgnoreProperties
    private Certification certification;

    // FK → tbl_certification_master
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_device_id")
    @JsonIgnoreProperties
    private MedicalDeviceType MedicalDeviceType;

    @Column(name = "certification_document_path")
    private String certificationDocumentPath;

    @Column(name = "country_of_origin")
    private String countryOfOrigin;

    @Column(name = "manufacturer_name")
    private String manufacturerName;

    @Column(name = "storage_condition")
    private String storageCondition;

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
