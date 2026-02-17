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
@Table(name = "pm_molecule")
public class Molecule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "molecule_id", nullable = false, unique = true)
    private Long moleculeId;

    @Column(name = "molecule_name")
    private String moleculeName;

    @Column(name = "mechanism_of_ction")
    private String mechanismOfAction;

    @Column(name = "primary_use")
    private String primaryUse;

}
