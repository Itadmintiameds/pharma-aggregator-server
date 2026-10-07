package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.ProductAttributeCosmeticandPersonalCare;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductAttributeCosmeticAndPersonalUseRepository
        extends JpaRepository<ProductAttributeCosmeticandPersonalCare, String> {
}
