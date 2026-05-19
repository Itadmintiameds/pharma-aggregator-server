package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.ServingSizeUnit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServingSizeUnitRepository extends JpaRepository<ServingSizeUnit, Long> {

    List<ServingSizeUnit> findByDosageForm_DosageId(Long id);

    List<ServingSizeUnit> findByProductForm_ProductFormId(Long id);
}
