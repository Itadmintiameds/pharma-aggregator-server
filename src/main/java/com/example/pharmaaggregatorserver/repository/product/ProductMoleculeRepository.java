package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.ProductMolecule;
import com.example.pharmaaggregatorserver.entity.product.ProductMoleculeId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductMoleculeRepository extends JpaRepository<ProductMolecule, ProductMoleculeId> {
}
