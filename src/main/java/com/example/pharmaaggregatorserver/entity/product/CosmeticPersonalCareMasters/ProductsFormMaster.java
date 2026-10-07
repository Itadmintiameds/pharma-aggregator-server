package com.example.pharmaaggregatorserver.entity.product.CosmeticPersonalCareMasters;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tbl_product_form_master")
public class ProductsFormMaster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "form_id")
    private Long formId;

    @Column(name = "form_name", nullable = false, unique = true, length = 50)
    private String formName;  // e.g. Liquid, Lotion, Serum, Oil, Spray...

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
