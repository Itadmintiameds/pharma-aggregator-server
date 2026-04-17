package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.MedicalDeviceProductMaster.DeviceCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceCategoryRepository extends JpaRepository<DeviceCategory, Long> {

    List<DeviceCategory> findByDeviceCategoryType(String deviceCategoryType);

    Optional<DeviceCategory> findByDeviceName(String deviceCat);
}
