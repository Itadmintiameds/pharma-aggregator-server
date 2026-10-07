package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.ProductAttributeFoodInfant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductAttributeFoodInfantRepository extends JpaRepository<ProductAttributeFoodInfant, String> {
}
