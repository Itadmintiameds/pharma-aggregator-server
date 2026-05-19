package com.example.pharmaaggregatorserver.entity.product;

import com.example.pharmaaggregatorserver.entity.product.MedicalDeviceProductMaster.*;
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

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tm_product_attribute_non_consumable_medical")
public class ProductAttributeNonConsumableMedical {

    @Id
    @Column(name = "product_attribute_id")
    private String productAttributeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_cat_id", nullable = false)
    @JsonIgnore
    private DeviceCategory deviceCategory;   // Device Category (BP Monitor / Glucometer / Thermometer / Nebulizer, etc.)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_sub_cat_id", nullable = false)
    @JsonIgnore
    private DeviceSubCategory deviceSubCategory;

    @Column(name = "brand_name", nullable = false, length = 100)
    private String brandName;       // Brand Name*

    @Column(name = "model_name", nullable = false, length = 60)
    private String modelName;

    @Column(name = "model_number", nullable = false, length = 60)
    private String modelNumber;     // Model Number*

    @Column(name = "device_classification", nullable = false, length = 100)
    private String deviceClassification;    // Device Classification (Class A/B/C/D)*

    @Column(name = "udi_number", length = 60)
    private String udiNumber;   // UDI (Unique Device Identifier)/ Serial Number  (if applicable)

    @Column(name = "purpose", nullable = false, columnDefinition = "TEXT")
    private String purpose;     // Intended Use / Purpose*

    @Column(name = "dimension_Size")
    private Double dimensionSize;       // Technical Dimensions / Capacity / Configuration*

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_specification_unit_id", nullable = true)
    @JsonIgnore
    private DeviceSpecificationUnit deviceSpecificationUnit;

    @Column(name = "key_features_specifications", columnDefinition = "TEXT", nullable = false)
    private String keyFeaturesSpecifications;  // Key Features / Technical Specifications*

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "certification_id", nullable = false)
//    @JsonIgnore
//    private Certification certification;        // Compliance Certificate* (anyone from the (CDSCO License No. (if required), ISO, CE, BIS))

    // Multiple certification types (CDSCO / ISO / CE / BIS)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "tm_non_consumable_medical_certifications",
            joinColumns = @JoinColumn(name = "product_attribute_id"),
            inverseJoinColumns = @JoinColumn(name = "certification_id")
    )
    @JsonIgnore
    private List<Certification> certifications = new ArrayList<>();

    @OneToMany(mappedBy = "nonConsumableMedical", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductCertificateDocument> certificateDocuments = new ArrayList<>();

//    @Column(name = "compliance_certificate_url")  // TODO : Does seller send multiple documents?
//    private String complianceCertificateUrl;   // Upload Compliance Certificate*

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "tm_product_non_consumable_material_mapping",
            joinColumns = @JoinColumn(name = "product_attribute_id"),
            inverseJoinColumns = @JoinColumn(name = "material_type_id")
    )
    @JsonIgnore
    private List<NonConsumableMaterialType> materialTypes = new ArrayList<>();     // Material / Build Type (Plastic, Metal, Steel)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "power_source_id")
    @JsonIgnore
    private PowerSource powerSource;

    @Column(name = "warranty_period", nullable = false, length = 5)
    private String warrantyPeriod;      // Only number allowed. • Max 3 chars.

    @Column(name = "service_availability", nullable = false)
    private boolean serviceAvailability = true;        // AMC / Service Availability* (Yes/No)

    // FK → tbl_device_category_master
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", nullable = false)
    @JsonIgnore
    private CountryMaster countryMaster;     // Country of Origin*

    @Column(name = "manufacturer_name", nullable = false, length = 100)
    private String manufacturerName;        // Manufacturer Name*

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storage_condition_id")
    @JsonIgnore
    private StorageConditionMaster storageConditionMaster;       // Storage Condition (If applicable)

    @Column(name = "brochure_path")
    private String brochurePath;        // Product Brochure / User Manual (PDF)*

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
