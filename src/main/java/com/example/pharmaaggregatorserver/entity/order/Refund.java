package com.example.pharmaaggregatorserver.entity.order;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Refund against a {@link Payment}. Null {@code orderItem} means a
 * whole-order refund; non-null scopes the refund to a single line
 * (e.g. a per-item return).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tbl_refund")
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refund_id")
    private Long refundId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    @JsonIgnore
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id")
    @JsonIgnore
    private OrderItem orderItem;

    @Column(name = "amount", precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    // BUYER / SELLER / ADMIN / SYSTEM
    @Column(name = "requested_by_role", length = 20)
    private String requestedByRole;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
