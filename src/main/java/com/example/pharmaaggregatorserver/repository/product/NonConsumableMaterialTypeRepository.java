package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.MedicalDeviceProductMaster.NonConsumableMaterialType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NonConsumableMaterialTypeRepository extends JpaRepository<NonConsumableMaterialType, Long> {
    Optional<NonConsumableMaterialType> findByMaterialTypeName(String trimmed);
}
