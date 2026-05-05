package com.example.pharmaaggregatorserver.entity.product;

import com.example.pharmaaggregatorserver.entity.product.MedicalDeviceProductMaster.Certification;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tm_product_certificate_document")
public class ProductCertificateDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productCertificateDocumentId;

    // Which certificate type this document belongs to (CDSCO / ISO / CE / BIS)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_id", nullable = false)
    @JsonIgnore
    private Certification certification;

    @Column(name = "certificate_url")
    private String certificateUrl;

    // Link back to non-consumable — nullable if consumable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "non_consumable_attribute_id")
    @JsonIgnore
    private ProductAttributeNonConsumableMedical nonConsumableMedical;

    // Link back to consumable — nullable if non-consumable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumable_attribute_id")
    @JsonIgnore
    private ProductAttributeConsumableMedical consumableMedical;

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
    @JoinColumn(name = "product_attribute_id")
    @JsonIgnore
    private ProductAttributeFoodInfant productAttributeFoodInfant;
}
