package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.MedicalDeviceProductMaster.CountryMaster;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryMasterRepository extends JpaRepository<CountryMaster, Long> {
}
