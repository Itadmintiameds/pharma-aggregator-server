package com.example.pharmaaggregatorserver.entity.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tm_age_group_master")
public class AgeGroupMaster {

    @Id
    @Column(name = "age_group_id")
    private Long ageGroupId;

    @Column(name = "age_group")
    private String ageGroup;
}
