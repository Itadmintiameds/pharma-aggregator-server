package com.example.pharmaaggregatorserver.controller.product;

import com.example.pharmaaggregatorserver.dto.product.*;
import com.example.pharmaaggregatorserver.service.product.MasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/masters")
@RequiredArgsConstructor
public class MastersController {

    private final MasterService masterService;

    @GetMapping("/countries")
    public List<CountryResponseDTO> getAllCountries() {
        return masterService.getAllCountries();
    }

    @GetMapping("/storagecondition")
    public List<StorageConditionResponseDTO> getAllStorageConditions() {
        return masterService.getAllStorageConditions();
    }

    @GetMapping("/material-types")
    public List<ConsumableMaterialTypeResponseDTO> getAllMaterialTypes() {
        return masterService.getAllMaterialTypes();
    }

    @GetMapping("/device-categories")
    public List<DeviceCategoryResponseDTO> getAllDeviceCategories() {
        return masterService.getAllDeviceCategories();
    }

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
}