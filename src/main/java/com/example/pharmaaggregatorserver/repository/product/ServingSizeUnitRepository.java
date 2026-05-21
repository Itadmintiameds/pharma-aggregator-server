package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.ServingSizeUnit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServingSizeUnitRepository extends JpaRepository<ServingSizeUnit, Long> {

    List<ServingSizeUnit> findByDosageForm_DosageId(Long id);

    List<ServingSizeUnit> findByProductForm_ProductFormId(Long id);

    Optional<ServingSizeUnit> findByServingSizeUnitIgnoreCaseAndDosageForm_DosageId(String servingSizeUnit, Long dosageId);

    Optional<ServingSizeUnit> findByServingSizeUnitIgnoreCaseAndProductForm_ProductFormId(String servingSizeUnit, Long productFormId);

}
