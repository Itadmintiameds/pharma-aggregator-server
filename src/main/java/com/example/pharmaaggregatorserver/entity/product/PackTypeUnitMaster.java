package com.example.pharmaaggregatorserver.entity.product;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tm_pack_type_unit_master")
public class PackTypeUnitMaster {

    @Id
    @Column(name = "pack_type_unit_id")
    private Long packTypeUnitId;

    @Column(name = "pack_type_unit_name", length = 20)
    private String packTypeUnitName;

    @Column(name = "is_active")
    private Boolean isActive = true;

}

