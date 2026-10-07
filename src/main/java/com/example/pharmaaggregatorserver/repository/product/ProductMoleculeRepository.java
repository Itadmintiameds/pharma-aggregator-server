package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.ProductMolecule;
import com.example.pharmaaggregatorserver.entity.product.ProductMoleculeId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductMoleculeRepository extends JpaRepository<ProductMolecule, ProductMoleculeId> {

    Optional<ProductMolecule> findByMolecule_MoleculeIdAndProductAttributeDrug_ProductAttributeId(
            Long moleculeId, String productAttributeId);
}
