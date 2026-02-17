package com.example.pharmaaggregatorserver.entity.temp.seller;

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
@Table(name = "tbl_temp_seller_coordinator")
public class TempSellerCoordinator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "temp_seller_coordinator_id")
    private Long tempSellerCoordinatorId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "temp_seller_id", unique = true, nullable = false)
    @JsonIgnore
    private TempSeller seller;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "designation", nullable = false, length = 100)
    private String designation;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "mobile", nullable = false, length = 100)
    private String mobile;

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
}
