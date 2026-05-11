package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.MedicalDeviceProductMaster.CountryMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CountryMasterRepository extends JpaRepository<CountryMaster, Long> {
    Optional<CountryMaster> findByCountryNameIgnoreCase(String country);
}
