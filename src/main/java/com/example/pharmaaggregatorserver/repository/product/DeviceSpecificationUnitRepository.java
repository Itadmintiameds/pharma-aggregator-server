package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.MedicalDeviceProductMaster.DeviceSpecificationUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceSpecificationUnitRepository extends JpaRepository<DeviceSpecificationUnit, Long> {

    List<DeviceSpecificationUnit> findByDeviceSubCategory_DeviceSubCatId(Long deviceSubCatId);

    // Used during import to resolve unit by sub-category + name
    Optional<DeviceSpecificationUnit> findByDeviceSubCategory_DeviceSubCatIdAndUnitNameIgnoreCase(
            Long deviceSubCatId, String unitName);
}