package com.example.pharmaaggregatorserver.entity.content;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_legal_content")
@Getter
@Setter
public class LegalContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "legal_content_id")
    private Long legalContentId;

    @Column(name = "content_key", nullable = false, unique = true, length = 100)
    private String contentKey;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "version", nullable = false)
    private Integer version = 1;

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
