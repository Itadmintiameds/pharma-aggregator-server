package com.example.pharmaaggregatorserver.entity.ifsc;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_ifsc_overrides")
@Data
public class IFSCOverride {
    @Id
    @Column(name = "ifsc_code", length = 11)
    private String ifscCode;

    @Column(name = "bank", length = 255)
    private String bank;

    @Column(name = "branch", length = 255)
    private String branch;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "district", length = 100)
    private String district;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
