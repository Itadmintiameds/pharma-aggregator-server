package com.example.pharmaaggregatorserver.entity.temp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "tbl_temp_seller_document")
public class TempSellerDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "temp_seller_documents_id")
    private Long tempSellerDocumentsId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "temp_seller_id", nullable = false)
    @JsonIgnore
    private TempSeller seller;

    @Column(name = "document_type", nullable = false, length = 100)
    private String documentType;

    @Column(name = "document_number", nullable = false, length = 100)
    private String documentNumber;

    @Column(name = "document_file_url", nullable = false)
    private String documentFileUrl;

//    TODO: does createdBy, updatedBy, createdAt, updatedAt required in all tables?

//    @Column(name = "created_by", length = 100)
//    private String createdBy;
//
//    @Column(name = "updated_by", length = 100)
//    private String updatedBy;
//
//    @CreationTimestamp
//    @Column(name = "created_at", updatable = false)
//    private LocalDateTime createdAt;
//
//    @UpdateTimestamp
//    @Column(name = "updated_at")
//    private LocalDateTime updatedAt;
}
