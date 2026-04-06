package com.example.pharmaaggregatorserver.entity.product.MedicalDeviceProductMaster;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_power_source_master")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PowerSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long powerSourceId;

    @Column(name = "source_name", nullable = false, unique = true, length = 100)
    private String powerSourceName;

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
}
