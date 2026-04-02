package com.example.pharmaaggregatorserver.entity.product;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductMoleculeId implements Serializable {

    @Column(name = "product_attribute_id")
    private String productAttributeId;

    @Column(name = "molecule_id")
    private Long moleculeId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductMoleculeId)) return false;
        ProductMoleculeId that = (ProductMoleculeId) o;
        return Objects.equals(productAttributeId, that.productAttributeId) &&
                Objects.equals(moleculeId, that.moleculeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productAttributeId, moleculeId);
    }
}