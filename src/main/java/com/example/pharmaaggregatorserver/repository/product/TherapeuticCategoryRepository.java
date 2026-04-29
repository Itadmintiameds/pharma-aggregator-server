package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.TherapeuticCategoryMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TherapeuticCategoryRepository extends JpaRepository<TherapeuticCategoryMaster, String> {

    Optional<TherapeuticCategoryMaster> findByTherapeuticCategory(String therapeuticCategory);

    List<TherapeuticCategoryMaster> findByCategory_CategoryId(Long categoryId);
}
