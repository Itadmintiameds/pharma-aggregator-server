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
@Table(name = "tm_special_schemes")
public class SpecialSchemes {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "special_schemes_id", updatable = false, nullable = false)
    private String specialSchemesId;

    @Column(name = "scheme_name")
    private String schemeName;

//    @Column(name = "schemeType")
//    private String schemeType;

    @Column(name = "buy_quantity")
    private Long buyQuantity;

    @Column(name = "free_quantity")
    private Long freeQuantity;

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
