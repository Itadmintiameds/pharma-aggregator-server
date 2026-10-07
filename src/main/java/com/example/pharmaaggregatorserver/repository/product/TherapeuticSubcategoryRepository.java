package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.TherapeuticSubcategoryMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TherapeuticSubcategoryRepository extends JpaRepository<TherapeuticSubcategoryMaster, String> {

    List<TherapeuticSubcategoryMaster>
    findByTherapeuticCategoryMaster_TherapeuticCategoryId(String therapeuticCategoryId);

    Optional<TherapeuticSubcategoryMaster> findByTherapeuticSubcategory(String therapeuticSubcategory);

    Optional<TherapeuticSubcategoryMaster> findByTherapeuticSubcategoryIgnoreCaseAndTherapeuticCategoryMaster_TherapeuticCategoryId(String therapeuticSubcategory, String therapeuticCategoryId);
}
