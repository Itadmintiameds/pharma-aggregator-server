package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.ProductAttributeNonConsumableMedical;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductAttributeNonConsumableMedicalRepository
        extends JpaRepository<ProductAttributeNonConsumableMedical, String> {
}
