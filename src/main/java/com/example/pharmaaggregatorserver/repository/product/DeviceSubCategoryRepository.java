package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.MedicalDeviceProductMaster.DeviceSubCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeviceSubCategoryRepository extends JpaRepository<DeviceSubCategory, Long> {
    List<DeviceSubCategory> findByIsActiveTrue();
    List<DeviceSubCategory> findByDeviceCategory_DeviceCatId(Long deviceCatId);
}
