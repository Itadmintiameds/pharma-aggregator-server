package com.example.pharmaaggregatorserver.entity.product.MedicalDeviceProductMaster;

import com.example.pharmaaggregatorserver.entity.product.Category;
import com.example.pharmaaggregatorserver.entity.product.ProductAttributeDrug;
import com.example.pharmaaggregatorserver.entity.product.ProductAttributeFoodInfant;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "tbl_storage_condition_master")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StorageConditionMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "storage_condition_id")
    private Long storageConditionId;

    @Column(name = "condition_name", nullable = false, length = 100)
    private String conditionName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "temperature_range")
    private String temperatureRange;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = true)
    @JsonIgnore
    private Category category;

    @ManyToMany(mappedBy = "storageConditions")
    @JsonIgnore
    private Set<ProductAttributeDrug> products = new HashSet<>();

    @OneToMany(mappedBy = "storageConditionMaster")
    @JsonIgnore
    private List<ProductAttributeFoodInfant> productAttributeFoodInfants;




}