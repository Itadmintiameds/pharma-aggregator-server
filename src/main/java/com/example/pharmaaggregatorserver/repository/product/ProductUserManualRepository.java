package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.ProductUserManual;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProductUserManualRepository extends JpaRepository <ProductUserManual, UUID> {

    Optional<ProductUserManual> findByProductAttributeDrug_ProductAttributeId(String productAttributeId);

}
