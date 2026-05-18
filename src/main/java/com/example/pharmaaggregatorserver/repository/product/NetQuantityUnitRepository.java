package com.example.pharmaaggregatorserver.repository.product;


import com.example.pharmaaggregatorserver.entity.product.NetQuantityUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NetQuantityUnitRepository extends JpaRepository<NetQuantityUnit, Long> {

    Optional<NetQuantityUnit> findByUnitNameIgnoreCase(String unitName);

    Optional<NetQuantityUnit> findByUnitSymbolIgnoreCase(String unitSymbol);

    List<NetQuantityUnit> findByUnitTypeIgnoreCase(String unitType);

    List<NetQuantityUnit> findByIsActiveTrue();
}