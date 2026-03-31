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
@Table(name = "tbl_dosage_form_master")
public class DosageForm {

    @Id
    @Column(name = "dosage_id")
    private Long dosageId;

    @Column(name = "dosage_name")
    private String dosageName;

    @OneToMany(mappedBy = "dosageForm", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<PackType> packTypes;
}
