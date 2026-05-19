package com.example.pharmaaggregatorserver.service.product;

import com.example.pharmaaggregatorserver.dto.product.*;
import com.example.pharmaaggregatorserver.entity.product.CosmeticPersonalCareMasters.HairType;
import com.example.pharmaaggregatorserver.entity.product.CosmeticPersonalCareMasters.IntendedUseArea;
import com.example.pharmaaggregatorserver.entity.product.CosmeticPersonalCareMasters.SkinType;
import com.example.pharmaaggregatorserver.entity.product.MedicalDeviceProductMaster.*;
import com.example.pharmaaggregatorserver.mapper.product.DeviceSpecificationUnitMapper;
import com.example.pharmaaggregatorserver.repository.product.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MasterService {

    private final CountryMasterRepository countryRepository;
    private final StorageConditionMasterRepository storageConditionRepository;
    private final ConsumableMaterialTypeRepository consumableMaterialTypeRepository;
    private final DeviceCategoryRepository deviceCategoryRepository;
    private final DeviceSubCategoryRepository deviceSubCategoryRepository;
    private final NonConsumableMaterialTypeRepository nonConsumableMaterialTypeRepository;
    private final CertificationRepository certificationRepository;
    private final PowerSourceMasterRepository powerSourceRepository;
    private final intendedUseAreaRepository intendedUseAreaRepository;
    private final hairTypeRepository hairTypeRepository;
    private final skinTypeRepository skinTypeRepository;
    private final DeviceSpecificationUnitRepository deviceSpecificationUnitRepository;
    private final DeviceSpecificationUnitMapper deviceSpecificationUnitMapper;

    public List<DeviceSpecificationUnitDto> getBySubCategory(Long subCatId) {
        return deviceSpecificationUnitRepository.findByDeviceSubCategory_DeviceSubCatId(subCatId)
                .stream()
                .map(deviceSpecificationUnitMapper::toDto)
                .toList();
    }

    public List<CountryResponseDTO> getAllCountries() {
        return countryRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private CountryResponseDTO convertToDTO(CountryMaster country) {
        return CountryResponseDTO.builder()
                .countryId(country.getCountryId())
                .countryName(country.getCountryName())
                .countryCode(country.getCountryCode())
                .phoneCode(country.getPhoneCode())
                .isActive(country.getIsActive())
                .build();
    }

    //storagecondition
    public List<StorageConditionResponseDTO> getAllStorageConditions() {
        return storageConditionRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private StorageConditionResponseDTO convertToDTO(StorageConditionMaster storageCondition) {
        return StorageConditionResponseDTO.builder()
                .storageConditionId(storageCondition.getStorageConditionId())
                .conditionName(storageCondition.getConditionName())
                .description(storageCondition.getDescription())
                .temperatureRange(storageCondition.getTemperatureRange())
                .displayOrder(storageCondition.getDisplayOrder())
                .isActive(storageCondition.getIsActive())
                .build();
    }

    //Material Type
    public List<ConsumableMaterialTypeResponseDTO> getAllConsumableMaterialTypes() {
        return consumableMaterialTypeRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ConsumableMaterialTypeResponseDTO convertToDTO(ConsumableMaterialType materialType) {
        return ConsumableMaterialTypeResponseDTO.builder()
                .materialTypeId(materialType.getMaterialTypeId())
                .materialTypeName(materialType.getMaterialTypeName())
                .description(materialType.getDescription())
                .build();
    }


    public List<DeviceCategoryResponseDTO> getAllDeviceCategories() {
        List<DeviceCategory> categories = deviceCategoryRepository.findAll();
        return categories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private DeviceCategoryResponseDTO convertToDTO(DeviceCategory category) {
        DeviceCategoryResponseDTO dto = new DeviceCategoryResponseDTO();
        dto.setDeviceCatId(category.getDeviceCatId());
        dto.setDeviceName(category.getDeviceName());
        dto.setDeviceCategoryType(category.getDeviceCategoryType());
        dto.setIsActive(category.getIsActive());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());
        return dto;
    }

    // GET ALL Device Sub Categories
    public List<DeviceSubCategoryResponseDTO> getAllDeviceSubCategories() {
        List<DeviceSubCategory> subCategories = deviceSubCategoryRepository.findAll();
        return subCategories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // GET BY ID Device Sub Category
    // GET BY CATEGORY ID - Get all sub categories for a specific category
    public List<DeviceSubCategoryResponseDTO> getDeviceSubCategoriesByCategoryId(Long categoryId) {
        List<DeviceSubCategory> subCategories = deviceSubCategoryRepository.findByDeviceCategory_DeviceCatId(categoryId);
        System.out.println("Sub categories found for category " + categoryId + ": " + subCategories.size()); // Debug log
        return subCategories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private DeviceSubCategoryResponseDTO convertToDTO(DeviceSubCategory subCategory) {
        return DeviceSubCategoryResponseDTO.builder()
                .deviceSubCatId(subCategory.getDeviceSubCatId())
                .deviceCatId(subCategory.getDeviceCategory().getDeviceCatId())
                .deviceCategoryName(subCategory.getDeviceCategory().getDeviceName())
                .subCategoryName(subCategory.getSubCategoryName())
                .description(subCategory.getDescription())
                .isActive(subCategory.getIsActive())
                .createdAt(subCategory.getCreatedAt())
                .updatedAt(subCategory.getUpdatedAt())
                .build();
    }

    public List<NonConsumableMaterialTypeDto> getAllNonConsumableMaterialTypes() {
        return nonConsumableMaterialTypeRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private NonConsumableMaterialTypeDto convertToDTO(NonConsumableMaterialType materialType) {
        NonConsumableMaterialTypeDto nonConsumableMaterialTypeDto = new NonConsumableMaterialTypeDto();
        nonConsumableMaterialTypeDto.setMaterialTypeId(materialType.getMaterialTypeId());
        nonConsumableMaterialTypeDto.setMaterialTypeName(materialType.getMaterialTypeName());
        return nonConsumableMaterialTypeDto;
    }

    public List<DeviceCategoryResponseDTO> getDeviceCategoriesByType(String deviceCategoryType) {
        return deviceCategoryRepository.findByDeviceCategoryType(deviceCategoryType)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<CertificationDto> getAllCertifications() {
        return certificationRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    public List<CertificationDto> getCertificationsByCategoryId(Long categoryId) {
        List<Certification> certifications = certificationRepository.findByCategory_CategoryId(categoryId);

        return certifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private CertificationDto convertToDTO(Certification certification) {
        return CertificationDto.builder()
                .certificationId(certification.getCertificationId())
                .certificationName(certification.getCertificationName())
                .categoryId(certification.getCategory().getCategoryId())
                .categoryName(certification.getCategory().getCategoryName())
                .build();
    }

    public List<PowerSourceDto> getAllPowerSources() {
        return powerSourceRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private PowerSourceDto convertToDTO(PowerSource powerSource) {
        return PowerSourceDto.builder()
                .powerSourceId(powerSource.getPowerSourceId())
                .powerSourceName(powerSource.getPowerSourceName())
                .build();
    }

    // Get all intended use areas
    public List<IntendedUseAreaResponseDTO> getAllIntendedUseAreas() {
        return intendedUseAreaRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private IntendedUseAreaResponseDTO convertToDTO(IntendedUseArea area) {
        return IntendedUseAreaResponseDTO.builder()
                .useAreaId(area.getUseAreaId())
                .areaName(area.getAreaName())
                .areaCode(area.getAreaCode())
                .description(area.getDescription())
                .iconClass(area.getIconClass())
                .displayOrder(area.getDisplayOrder())
                .isActive(area.getIsActive())
                .createdAt(area.getCreatedAt())
                .updatedAt(area.getUpdatedAt())
                .build();
    }

    // Get all skin/hair types
//    public List<SkinHairTypeResponseDTO> getAllSkinHairTypes() {
//        return skinHairTypeRepository.findAll()
//                .stream()
//                .map(this::convertToDTO)
//                .collect(Collectors.toList());
//    }
//
//    private SkinHairTypeResponseDTO convertToDTO(SkinHairType skinHairType) {
//        return SkinHairTypeResponseDTO.builder()
//                .typeId(skinHairType.getTypeId())
//                .category(skinHairType.getCategory().name())
//                .typeName(skinHairType.getTypeName())
//                .typeCode(skinHairType.getTypeCode())
//                .description(skinHairType.getDescription())
//                .displayOrder(skinHairType.getDisplayOrder())
//                .iconClass(skinHairType.getIconClass())
//                .isActive(skinHairType.getIsActive())
//                .createdAt(skinHairType.getCreatedAt())
//                .updatedAt(skinHairType.getUpdatedAt())
//                .build();
//    }
    // Get all hair types
    public List<HairTypeResponseDTO> getAllHairTypes() {
        return hairTypeRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private HairTypeResponseDTO convertToDTO(HairType hairType) {
        return HairTypeResponseDTO.builder()
                .typeId(hairType.getTypeId())
                .typeName(hairType.getTypeName())
                .typeCode(hairType.getTypeCode())
                .description(hairType.getDescription())
                .displayOrder(hairType.getDisplayOrder())
                .iconClass(hairType.getIconClass())
                .isActive(hairType.getIsActive())
                .createdAt(hairType.getCreatedAt())
                .updatedAt(hairType.getUpdatedAt())
                .build();
    }

    // Get all skin types
    public List<SkinTypeResponseDTO> getAllSkinTypes() {
        return skinTypeRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private SkinTypeResponseDTO convertToDTO(SkinType skinType) {
        return SkinTypeResponseDTO.builder()
                .typeId(skinType.getSkintypeId())
                .typeName(skinType.getTypeName())
                .typeCode(skinType.getTypeCode())
                .description(skinType.getDescription())
                .displayOrder(skinType.getDisplayOrder())
                .iconClass(skinType.getIconClass())
                .isActive(skinType.getIsActive())
                .createdAt(skinType.getCreatedAt())
                .updatedAt(skinType.getUpdatedAt())
                .build();
    }


}