package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.DrugCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DrugCategoryRepository extends JpaRepository <DrugCategory, String> {
}
