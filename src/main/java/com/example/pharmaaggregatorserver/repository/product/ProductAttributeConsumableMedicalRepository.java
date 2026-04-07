package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.ProductAttributeConsumableMedical;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductAttributeConsumableMedicalRepository
        extends JpaRepository<ProductAttributeConsumableMedical, String> {
}
