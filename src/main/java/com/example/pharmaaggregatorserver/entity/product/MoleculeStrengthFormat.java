package com.example.pharmaaggregatorserver.entity.product;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "tm_molecule_strength_format")
public class MoleculeStrengthFormat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "molecule_strength_id")
    private Long moleculeStrengthId;

    @Column(name = "molecule_strength_format")
    private String moleculeStrengthFormat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dosage_id", nullable = false)
    @JsonIgnore
    private DosageForm dosageForm;

}
