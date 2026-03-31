package com.example.pharmaaggregatorserver.entity.product;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tm_additional_discount")
public class AdditionalDiscount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "additional_discount_id", updatable = false, nullable = false)
    private String additionalDiscountId;

    @Column(name = "minimum_purchase_quantity")
    private Long minimumPurchaseQuantity;

    @Column(name = "additional_discount_percentage")
    private Long additionalDiscountPercentage;

    @Column(name = "effective_start_date")
    private LocalDate effectiveStartDate;

    @Column(name = "effective_start_time")
    private LocalTime effectiveStartTime;

    @Column(name = "effective_end_date")
    private LocalDate effectiveEndDate ;

    @Column(name = "effective_end_time")
    private LocalTime effectiveEndTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pricing_id", nullable = false)
    @JsonIgnore
    private PricingDetails pricingDetails;
}
