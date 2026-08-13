package com.example.pharmaaggregatorserver.entity.master;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_buyer_type_master")
@Getter
@Setter
public class BuyerTypeMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "buyer_type_id")
    private Long buyerTypeId;

    @Column(name = "buyer_type_name", nullable = false, unique = true, length = 100)
    private String buyerTypeName;

    @Column(name = "buyer_type_abbreviation", nullable = false, unique = true, length = 100)
    private String buyerTypeAbbreviation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mandatory_document_type_id")
    private DocumentTypeMaster mandatoryDocumentTypeId;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
