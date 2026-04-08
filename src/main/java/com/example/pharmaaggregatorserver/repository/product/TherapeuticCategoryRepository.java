package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.TherapeuticCategoryMaster;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TherapeuticCategoryRepository extends JpaRepository<TherapeuticCategoryMaster, String> {
}
