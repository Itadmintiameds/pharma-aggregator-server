package com.example.pharmaaggregatorserver.entity.product.MedicalDeviceProductMaster;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tm_certificate_document")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificateDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "certificate_document_id")
    private String certificateDocumentId;

    // File path (S3 / local / URL)
    @Column(name = "document_path", nullable = false)
    private String documentPath;

    @Column(name = "document_type")
    private String documentType; // PDF, JPG, etc.

    @Column(name = "uploaded_by")
    private String uploadedBy;

    @Column(name = "uploaded_date")
    private LocalDateTime uploadedDate;

    // 🔗 LINK TO CERTIFICATION
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_id", nullable = false)
    @JsonIgnore
    private Certification certification;
}
