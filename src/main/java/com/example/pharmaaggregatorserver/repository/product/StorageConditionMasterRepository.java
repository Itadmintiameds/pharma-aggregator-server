package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.MedicalDeviceProductMaster.StorageConditionMaster;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StorageConditionMasterRepository extends JpaRepository<StorageConditionMaster, Long> {
}
