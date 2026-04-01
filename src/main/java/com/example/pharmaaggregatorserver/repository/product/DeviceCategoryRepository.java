package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.MedicalDeviceProductMaster.DeviceCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceCategoryRepository extends JpaRepository<DeviceCategory, Long> {
}
