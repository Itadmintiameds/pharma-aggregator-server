package com.example.pharmaaggregatorserver.entity.buyer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "tbl_buyer_delivery_address")
public class BuyerDeliveryAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_address_id")
    private Long deliveryAddressId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buyer_id", nullable = false)
    @JsonIgnore
    private Buyer buyer;

    @Column(name = "label", length = 100)
    private String label;

    @Column(name = "state")
    private String state;

    @Column(name = "district")
    private String district;

    @Column(name = "taluka")
    private String taluka;

    @Column(name = "city")
    private String city;

    @Column(name = "street")
    private String street;

    @Column(name = "building_no")
    private String buildingNo;

    @Column(name = "landmark")
    private String landmark;

    @Column(name = "pinCode")
    private String pinCode;

    @Column(name = "is_default")
    private boolean isDefault;

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

    @Column(name = "is_active")
    private Boolean isActive = true;
}
