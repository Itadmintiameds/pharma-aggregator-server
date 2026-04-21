package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.MedicalDeviceProductMaster.ConsumableMaterialType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConsumableMaterialTypeRepository extends JpaRepository<ConsumableMaterialType, Long> {
    Optional<ConsumableMaterialType> findByMaterialTypeName(String trim);
}
