package com.example.pharmaaggregatorserver.entity.temp.buyer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_temp_buyer_review_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TempBuyerReviewHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "temp_buyer_id", nullable = false)
    @JsonIgnore
    private TempBuyer tempBuyer;

    @Column(nullable = false, length = 100)
    private String status; // CORRECTION_REQUIRED, REJECTED, APPROVED

    @Column(columnDefinition = "TEXT")
    private String comments;

    @Column(name = "reviewed_by", length = 100)
    private String reviewedBy;

    @Column(name = "reviewed_at")
    @CreationTimestamp
    private LocalDateTime reviewedAt;
}
