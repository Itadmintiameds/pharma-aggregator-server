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
@Table(name = "pm_product_molecule")
public class ProductMolecule {

    @EmbeddedId
    private ProductMoleculeId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productAttributeId")
    @JoinColumn(name = "product_attribute_id")
    private ProductAttributeDrug productAttributeDrug;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("moleculeId")
    @JoinColumn(name = "molecule_id")
    private Molecule molecule;

    @Column(name = "strength")
    private String strength;
}
