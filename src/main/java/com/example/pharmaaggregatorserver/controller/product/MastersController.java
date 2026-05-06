package com.example.pharmaaggregatorserver.controller.product;

import com.example.pharmaaggregatorserver.dto.product.*;
import com.example.pharmaaggregatorserver.service.product.MasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/masters")
@RequiredArgsConstructor
public class MastersController {

    private final MasterService masterService;

    @GetMapping("/countries")
    public ResponseEntity<List<CountryResponseDTO>> getAllCountries() {
        return ResponseEntity.ok(masterService.getAllCountries());
    }

    @GetMapping("/storagecondition")
    public ResponseEntity<List<StorageConditionResponseDTO>> getAllStorageConditions() {
        return ResponseEntity.ok(masterService.getAllStorageConditions());
    }

    @GetMapping("/consumable-material-types")
    public ResponseEntity<List<ConsumableMaterialTypeResponseDTO>> getAllConsumableMaterialTypes() {
        return ResponseEntity.ok(masterService.getAllConsumableMaterialTypes());
    }

    @GetMapping("/device-categories")
    public ResponseEntity<List<DeviceCategoryResponseDTO>> getAllDeviceCategories() {
        return ResponseEntity.ok(masterService.getAllDeviceCategories());
    }

    @GetMapping("/intended-use-areas")
    public ResponseEntity<List<IntendedUseAreaResponseDTO>> getAllIntendedUseAreas() {
        return ResponseEntity.ok(masterService.getAllIntendedUseAreas());
    }

   // GET all skin/hair types
//    @GetMapping("/skin-hair-types")
//    public ResponseEntity<List<SkinHairTypeResponseDTO>> getAllSkinHairTypes() {
//        return ResponseEntity.ok(masterService.getAllSkinHairTypes());
//    }

    // GET ALL Device Sub Categories
    @GetMapping("/device-sub-categories")
    public ResponseEntity<List<DeviceSubCategoryResponseDTO>> getAllDeviceSubCategories() {
        List<DeviceSubCategoryResponseDTO> subCategories = masterService.getAllDeviceSubCategories();
        return ResponseEntity.ok(subCategories);
    }

    // GET BY CATEGORY ID - Get all sub categories for a specific category
    @GetMapping("/device-sub-categories/{categoryId}")
    public ResponseEntity<List<DeviceSubCategoryResponseDTO>> getDeviceSubCategoriesByCategoryId(@PathVariable Long categoryId) {
        List<DeviceSubCategoryResponseDTO> subCategories = masterService.getDeviceSubCategoriesByCategoryId(categoryId);
        return ResponseEntity.ok(subCategories);
    }

    @GetMapping("/non-consumable-material-types")
    public ResponseEntity<List<NonConsumableMaterialTypeDto>> getAllNonConsumableMaterialTypes() {
        return ResponseEntity.ok(masterService.getAllNonConsumableMaterialTypes());
    }

    @GetMapping("/device-categories/{deviceCategoryType}")
    public ResponseEntity<List<DeviceCategoryResponseDTO>> getDeviceCategoriesByType(@PathVariable String deviceCategoryType) {
        return ResponseEntity.ok(masterService.getDeviceCategoriesByType(deviceCategoryType));
    }

    @GetMapping("/certifications")
    public ResponseEntity<List<CertificationDto>> getAllCertifications() {
        return ResponseEntity.ok(masterService.getAllCertifications());
    }

    @GetMapping("/certifications/categoryId/{categoryId}")
    public ResponseEntity<List<CertificationDto>> getCertificationsByCategoryId(@PathVariable("categoryId") Long categoryId) {
        return ResponseEntity.ok(masterService.getCertificationsByCategoryId(categoryId));
    }

    @GetMapping("/power-sources")
    public ResponseEntity<List<PowerSourceDto>> getAllPowerSources() {
        return ResponseEntity.ok(masterService.getAllPowerSources());
    }


}