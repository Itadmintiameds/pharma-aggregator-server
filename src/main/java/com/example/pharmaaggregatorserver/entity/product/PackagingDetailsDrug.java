package com.example.pharmaaggregatorserver.entity.product;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pm_packaging_details_drug")
public class PackagingDetailsDrug {

    @Id
    @Column(name = "packaging_id")
    private String packagingId;

    @Column(name = "packaging_unit")
    private String packagingUnit;

    @Column(name = "number_of_units")
    private Long numberOfUnits;

    @Column(name = "pack_size")
    private Long packSize;

    @Column(name = "minimum_order_quantity")
    private Long minimumOrderQuantity;

    @Column(name = "maximum_order_quantity")
    private Long maximumOrderQuantity;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "modified_by")
    private Long modifiedBy;

    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    @OneToOne
    @JoinColumn(name = "product_id")
    private ProductDetailsDrug product;

}
