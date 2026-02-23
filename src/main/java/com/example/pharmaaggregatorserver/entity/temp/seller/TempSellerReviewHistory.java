package com.example.pharmaaggregatorserver.entity.temp.seller;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_temp_seller_review_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TempSellerReviewHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "temp_seller_id", nullable = false)
    private TempSeller tempSeller;

    @Column(nullable = false, length = 100)
    private String status; // CORRECTION_REQUIRED, REJECTED, APPROVED

    @Column(columnDefinition = "TEXT")
    private String comments;

    @Column(name = "reviewed_by", length = 100)
    private String reviewedBy; // admin username/ID later

    @Column(name = "reviewed_at")
    @CreationTimestamp
    private LocalDateTime reviewedAt;
}
