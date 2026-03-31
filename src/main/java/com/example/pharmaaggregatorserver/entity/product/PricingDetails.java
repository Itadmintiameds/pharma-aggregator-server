package com.example.pharmaaggregatorserver.entity.product;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tm_pricing_details")
public class PricingDetails {

    @Id
    @Column(name = "pricing_id")
    private String pricingId;

    @Column(name = "batch_lot_number")
    private String batchLotNumber;

    @Column(name = "manufacturer_name")
    private String manufacturerName;

    @Column(name = "manufacturing_date")
    private LocalDateTime manufacturingDate;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "storage_condition")
    private String storageCondition;

    @Column(name = "stock_quantity")
    private Long stockQuantity;

    @Column(name = "price_per_unit")
    private Long pricePerUnit;

    @Column(name = "mrp")
    private Long mrp;

    @Column(name = "discount_percentage")
    private Long discountPercentage;

    @Column(name = "gst_percentage")
    private Long gstPercentage;

    @Column(name = "final_price")
    private Long finalPrice;

    @Column(name = "hsn_code")
    private Long hsnCode;

    @Column(name = "shelf_life_months")
    private Long shelfLifeMonths;

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

    @OneToMany(mappedBy = "pricingDetails", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<AdditionalDiscount> additionalDiscounts;

}
