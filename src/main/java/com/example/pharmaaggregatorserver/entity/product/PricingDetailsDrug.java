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
@Table(name = "pm_pricing_details_drug")
public class PricingDetailsDrug {

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

    @Column(name = "minimum_purchase_quantity")
    private Long minimumPurchaseQuantity;

    @Column(name = "additional_discount")
    private Long additionalDiscount;

    @Column(name = "final_price")
    private Long finalPrice;

    @Column(name = "hsn_code")
    private Long hsnCode;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "modified_by")
    private Long modifiedBy;

    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductDetailsDrug product;


}
