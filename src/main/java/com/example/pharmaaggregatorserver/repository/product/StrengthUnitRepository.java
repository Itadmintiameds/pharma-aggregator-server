package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.StrengthUnit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StrengthUnitRepository extends JpaRepository<StrengthUnit, Long> {

    List<StrengthUnit> findByCategory_CategoryId(Long categoryId);

    Optional<StrengthUnit> findByUnitNameIgnoreCaseAndCategory_CategoryId(String unitName, Long categoryId);
}
