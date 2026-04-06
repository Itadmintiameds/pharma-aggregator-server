package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.MedicalDeviceProductMaster.DimensionSize;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DimensionRepository extends JpaRepository<DimensionSize, Long> {
}