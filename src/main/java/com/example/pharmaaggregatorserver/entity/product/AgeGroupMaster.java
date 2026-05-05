package com.example.pharmaaggregatorserver.entity.product;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

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

//    @OneToMany(mappedBy = "ageGroupMaster", cascade = CascadeType.ALL, orphanRemoval = true)
//    @JsonIgnore
//    private Set<ProductAttributeFoodInfant> productAttributeFoodInfants;
}
