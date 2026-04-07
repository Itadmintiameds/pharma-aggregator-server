package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.MedicalDeviceProductMaster.PowerSource;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PowerSourceMasterRepository extends JpaRepository<PowerSource, Long> {
}
