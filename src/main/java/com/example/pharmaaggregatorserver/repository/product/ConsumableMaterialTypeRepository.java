package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.MedicalDeviceProductMaster.ConsumableMaterialType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsumableMaterialTypeRepository extends JpaRepository<ConsumableMaterialType, Long> {
}
