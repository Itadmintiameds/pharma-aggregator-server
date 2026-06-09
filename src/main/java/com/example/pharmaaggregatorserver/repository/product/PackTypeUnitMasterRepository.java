package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.PackTypeUnitMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PackTypeUnitMasterRepository extends JpaRepository<PackTypeUnitMaster, Long> {
    Optional<PackTypeUnitMaster> findByPackTypeUnitNameIgnoreCase(String packTypeUnitName);
}

