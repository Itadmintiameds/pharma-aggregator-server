package com.example.pharmaaggregatorserver.entity.temp.seller;

import com.example.pharmaaggregatorserver.entity.master.DistrictMaster;
import com.example.pharmaaggregatorserver.entity.master.StateMaster;
import com.example.pharmaaggregatorserver.entity.master.TalukaMaster;
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
@Table(name = "tbl_temp_seller_address")
public class TempSellerAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seller_address_id")
    private Long tempSellerAddressId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", unique = true, nullable = false)
    @JsonIgnore
    private TempSeller seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_id")
    private StateMaster state;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id")
    private DistrictMaster district;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "taluka_id")
    private TalukaMaster taluka;

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

    //    TODO: does createdBy, updatedBy, createdAt, updatedAt required in all tables?
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
