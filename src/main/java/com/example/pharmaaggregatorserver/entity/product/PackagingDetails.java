package com.example.pharmaaggregatorserver.entity.product;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "tm_packaging_details")
public class PackagingDetails {

    @Id
    @Column(name = "packaging_id")
    private String packagingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pack_id", nullable = false)
    @JsonIgnore
    private PackType packType;

    @Column(name = "unit_per_pack")
    private Long unitPerPack;

    @Column(name = "number_of_packs")
    private Long numberOfPacks;

    @Column(name = "pack_size")
    private Long packSize;

    @Column(name = "minimum_order_quantity")
    private Long minimumOrderQuantity;

    @Column(name = "maximum_order_quantity")
    private Long maximumOrderQuantity;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "modified_by")
    private String modifiedBy;

    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore
    private ProductDetails productDetails;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pack_type_unit_id", nullable = true)
    @JsonIgnore
    private PackTypeUnitMaster packTypeUnitMaster;

}
