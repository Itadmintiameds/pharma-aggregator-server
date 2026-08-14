package com.example.pharmaaggregatorserver.entity.order;

import com.example.pharmaaggregatorserver.entity.buyer.Buyer;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Parent order placed by a buyer. Fans out into one {@link SellerOrder} per
 * seller present in the cart at placement time. The delivery address fields
 * are a point-in-time snapshot (NOT a live FK into
 * {@code BuyerDeliveryAddress}) so a later address edit/deletion never
 * mutates history for an already-placed order.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tbl_order")
public class Order {

    @Id
    @Column(name = "order_id")
    private String orderId;

    // Buyer exposes passwordHash-bearing BuyerUser via Buyer#user with no
    // @JsonIgnore on that field's own chain guaranteed everywhere it's read,
    // so any relation into Buyer is proactively cut off from serialization
    // here — response DTOs are flat projections built in the service layer.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buyer_id", nullable = false)
    @JsonIgnore
    private Buyer buyer;

    @Column(name = "delivery_name")
    private String deliveryName;

    @Column(name = "delivery_phone")
    private String deliveryPhone;

    @Column(name = "delivery_address_line")
    private String deliveryAddressLine;

    @Column(name = "delivery_city")
    private String deliveryCity;

    @Column(name = "delivery_district")
    private String deliveryDistrict;

    @Column(name = "delivery_state")
    private String deliveryState;

    @Column(name = "delivery_pin_code")
    private String deliveryPinCode;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "item_count")
    private Integer itemCount;

    @Column(name = "seller_order_count")
    private Integer sellerOrderCount;

    @Column(name = "subtotal", precision = 14, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "shipping_total", precision = 14, scale = 2)
    private BigDecimal shippingTotal;

    @Column(name = "tax_total", precision = 14, scale = 2)
    private BigDecimal taxTotal;

    @Column(name = "grand_total", precision = 14, scale = 2)
    private BigDecimal grandTotal;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    @JsonIgnore
    private Payment payment;

    @Column(name = "placed_at")
    private LocalDateTime placedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancelled_by_role", length = 20)
    private String cancelledByRole;

    @Column(name = "cancel_reason", columnDefinition = "TEXT")
    private String cancelReason;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<SellerOrder> sellerOrders = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
