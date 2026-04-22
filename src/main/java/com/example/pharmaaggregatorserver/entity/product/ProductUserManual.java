package com.example.pharmaaggregatorserver.entity.product;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tm_product_user_manual")
public class ProductUserManual {

    @Id
    @UuidGenerator
    @Column(name = "user_manual_id", nullable = false, updatable = false)
    private UUID userManualId;

    @Column(name = "user_manual_url")
    private String userManualUrl;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_attribute_id", nullable = false)
    private ProductAttributeDrug productAttributeDrug;
}
