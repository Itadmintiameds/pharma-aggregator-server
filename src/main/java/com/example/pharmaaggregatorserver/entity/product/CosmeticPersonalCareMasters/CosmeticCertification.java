package com.example.pharmaaggregatorserver.entity.product.CosmeticPersonalCareMasters;
//CosmeticCertification

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tm_cosmetic_certification_master")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CosmeticCertification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "certification_id")
    private Long certificationId;

    @Column(name = "certification_name", nullable = false, unique = true, length = 100)
    private String certificationName;

    @Column(name = "certification_code", unique = true, length = 30)
    private String certificationCode;

    @Column(name = "certification_type", length = 50)
    @Enumerated(EnumType.STRING)
    private CertificationType type;

    @Column(name = "issuing_authority", length = 200)
    private String issuingAuthority;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "icon_class", length = 100)
    private String iconClass;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "is_mandatory")
    private Boolean isMandatory = false;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum CertificationType {
        QUALITY,           // ISO, GMP
        REGULATORY,        // FDA, CDSCO, AYUSH
        ETHICAL,          // Cruelty-Free, Vegan
        ORGANIC,          // Organic Certification
        SAFETY            // Dermatologically Tested
    }

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}