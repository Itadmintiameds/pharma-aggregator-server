package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.MedicalDeviceProductMaster.DeviceSpecificationUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceSpecificationUnitRepository extends JpaRepository<DeviceSpecificationUnit, Long> {

    // Instead of: findByDeviceSubCategory_SubCatId
    List<DeviceSpecificationUnit> findByDeviceSubCategory_DeviceSubCatId(Long deviceSubCatId);
}