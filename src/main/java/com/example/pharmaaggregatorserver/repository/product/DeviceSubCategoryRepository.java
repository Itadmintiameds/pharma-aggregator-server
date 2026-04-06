package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.MedicalDeviceProductMaster.DeviceSubCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceSubCategoryRepository extends JpaRepository<DeviceSubCategory, Long> {
}
