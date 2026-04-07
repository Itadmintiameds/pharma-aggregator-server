package com.example.pharmaaggregatorserver.mapper.product;

import com.example.pharmaaggregatorserver.dto.product.ConsumableProductAttributeDTO;
import com.example.pharmaaggregatorserver.dto.product.ProductCertificateDocumentDto;
import com.example.pharmaaggregatorserver.entity.product.MedicalDeviceProductMaster.*;
import com.example.pharmaaggregatorserver.entity.product.ProductAttributeConsumableMedical;
import com.example.pharmaaggregatorserver.entity.product.ProductCertificateDocument;
import com.example.pharmaaggregatorserver.repository.product.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductAttributeConsumableMedicalMapper {

    private final DeviceCategoryRepository deviceCategoryRepo;
    private final DeviceSubCategoryRepository deviceSubCategoryRepo;
    private final CertificationRepository certificationRepo;
    private final ConsumableMaterialTypeRepository consumableMaterialTypeRepo;
    private final CountryMasterRepository countryMasterRepo;
    private final StorageConditionMasterRepository storageConditionMasterRepo;

    // ================= DTO → ENTITY =================
    public ProductAttributeConsumableMedical toEntity(ConsumableProductAttributeDTO dto) {
        if (dto == null) return null;

        ProductAttributeConsumableMedical entity = new ProductAttributeConsumableMedical();

        entity.setProductAttributeId(dto.getProductAttributeId());
        entity.setBrandName(dto.getBrandName());
        entity.setSterileOrNonSterile(dto.getSterileOrNonSterile());
        entity.setDisposalOrReusable(dto.getDisposalOrReusable());
        entity.setShelfLife(dto.getShelfLife());
        entity.setDimensionSize(dto.getDimensionSize());
        entity.setPurpose(dto.getPurpose());
        entity.setKeyFeaturesSpecifications(dto.getKeyFeaturesSpecifications());
        entity.setSafetyInstructions(dto.getSafetyInstructions());
        entity.setManufacturerName(dto.getManufacturerName());
        entity.setBrochureType(dto.getBrochureType());
        entity.setBrochurePath(dto.getBrochurePath());

        // Device Category
        if (dto.getDeviceCatId() != null) {
            DeviceCategory deviceCategory = deviceCategoryRepo.findById(dto.getDeviceCatId())
                    .orElseThrow(() -> new RuntimeException(
                            "DeviceCategory not found: " + dto.getDeviceCatId()));
            entity.setDeviceCategory(deviceCategory);
        }

        // Device Sub Category
        if (dto.getDeviceSubCatId() != null) {
            DeviceSubCategory deviceSubCategory = deviceSubCategoryRepo.findById(dto.getDeviceSubCatId())
                    .orElseThrow(() -> new RuntimeException(
                            "DeviceSubCategory not found: " + dto.getDeviceSubCatId()));
            entity.setDeviceSubCategory(deviceSubCategory);
        }

        // Certification Documents
        if (dto.getCertificateDocuments() != null && !dto.getCertificateDocuments().isEmpty()) {
            List<Certification> certifications = new ArrayList<>();
            List<ProductCertificateDocument> documents = new ArrayList<>();

            for (ProductCertificateDocumentDto certDto : dto.getCertificateDocuments()) {
                Certification certification = certificationRepo.findById(certDto.getCertificationId())
                        .orElseThrow(() -> new RuntimeException(
                                "Certification not found: " + certDto.getCertificationId()));

                certifications.add(certification);

                ProductCertificateDocument doc = ProductCertificateDocument.builder()
                        .certification(certification)
                        .certificateUrl(certDto.getCertificateUrl())
                        .consumableMedical(entity)
                        .build();

                documents.add(doc);
            }

            entity.setCertifications(certifications);
            entity.setCertificateDocuments(documents);
        }

        // Country of origin
        if (dto.getCountryId() != null) {
            CountryMaster countryMaster = countryMasterRepo.findById(dto.getCountryId())
                    .orElseThrow(() -> new RuntimeException(
                            "Country not found: " + dto.getCountryId()));
            entity.setCountryMaster(countryMaster);
        }

        // Storage Category
        if (dto.getStorageConditionId() != null) {
            StorageConditionMaster storageConditionMaster = storageConditionMasterRepo.findById(dto.getStorageConditionId())
                    .orElseThrow(() -> new RuntimeException(
                            "StorageCondition not found: " + dto.getStorageConditionId()));
            entity.setStorageConditionMaster(storageConditionMaster);
        }

        // MATERIAL TYPES - Many-to-Many
        if (dto.getMaterialTypeId() != null && !dto.getMaterialTypeId().isEmpty()) {
            List<ConsumableMaterialType> materialTypes = consumableMaterialTypeRepo.findAllById(dto.getMaterialTypeId());

            if (materialTypes.size() != dto.getMaterialTypeId().size()) {
                List<Long> foundIds = materialTypes.stream()
                        .map(ConsumableMaterialType::getMaterialTypeId)
                        .collect(Collectors.toList());
                List<Long> notFoundIds = dto.getMaterialTypeId().stream()
                        .filter(id -> !foundIds.contains(id))
                        .collect(Collectors.toList());
                throw new RuntimeException("MaterialTypes not found: " + notFoundIds);
            }

            entity.setMaterialTypes(materialTypes);
        } else {
            entity.setMaterialTypes(new ArrayList<>());
        }

        return entity;
    }

    // ================= ENTITY → DTO =================
    public ConsumableProductAttributeDTO toDto(ProductAttributeConsumableMedical entity) {
        if (entity == null) return null;

        ConsumableProductAttributeDTO dto = new ConsumableProductAttributeDTO();

        dto.setProductAttributeId(entity.getProductAttributeId());
        dto.setBrandName(entity.getBrandName());
        dto.setSterileOrNonSterile(entity.getSterileOrNonSterile());
        dto.setDisposalOrReusable(entity.getDisposalOrReusable());
        dto.setShelfLife(entity.getShelfLife());
        dto.setDimensionSize(entity.getDimensionSize());
        dto.setPurpose(entity.getPurpose());
        dto.setKeyFeaturesSpecifications(entity.getKeyFeaturesSpecifications());
        dto.setSafetyInstructions(entity.getSafetyInstructions());
        dto.setManufacturerName(entity.getManufacturerName());
        dto.setBrochureType(entity.getBrochureType());
        dto.setBrochurePath(entity.getBrochurePath());

        // Device Category
        if (entity.getDeviceCategory() != null) {
            dto.setDeviceCatId(entity.getDeviceCategory().getDeviceCatId());
        }

        // Device Sub Category
        if (entity.getDeviceSubCategory() != null) {
            dto.setDeviceSubCatId(entity.getDeviceSubCategory().getDeviceSubCatId());
        }

        // Country
        if (entity.getCountryMaster() != null) {
            dto.setCountryId(entity.getCountryMaster().getCountryId());
        }

        // Storage Category
        if (entity.getStorageConditionMaster() != null) {
            dto.setStorageConditionId(entity.getStorageConditionMaster().getStorageConditionId());
        }

        // Certification - Map certificate documents back to DTOs (REMOVED DUPLICATE)
        if (entity.getCertificateDocuments() != null && !entity.getCertificateDocuments().isEmpty()) {
            List<ProductCertificateDocumentDto> certDtos = entity.getCertificateDocuments().stream()
                    .map(doc -> {
                        ProductCertificateDocumentDto certDto = new ProductCertificateDocumentDto();
                        if (doc.getCertification() != null) {
                            certDto.setProductCertificateDocumentId(doc.getProductCertificateDocumentId());
                            certDto.setCertificationId(doc.getCertification().getCertificationId());
                            certDto.setCertificationName(doc.getCertification().getCertificationName());
                        }
                        certDto.setCertificateUrl(doc.getCertificateUrl());
                        return certDto;
                    })
                    .collect(Collectors.toList());
            dto.setCertificateDocuments(certDtos);
        }

        // MATERIAL TYPES - Many-to-Many
        if (entity.getMaterialTypes() != null && !entity.getMaterialTypes().isEmpty()) {
            List<Long> materialTypeIds = entity.getMaterialTypes().stream()
                    .map(ConsumableMaterialType::getMaterialTypeId)
                    .collect(Collectors.toList());
            dto.setMaterialTypeId(materialTypeIds);

            List<ConsumableProductAttributeDTO.MaterialTypeInfo> materialTypeInfos =
                    entity.getMaterialTypes().stream()
                            .map(mt -> {
                                ConsumableProductAttributeDTO.MaterialTypeInfo info =
                                        new ConsumableProductAttributeDTO.MaterialTypeInfo();
                                info.setMaterialTypeId(mt.getMaterialTypeId());
                                info.setMaterialTypeName(mt.getMaterialTypeName());
                                return info;
                            })
                            .collect(Collectors.toList());
            dto.setMaterialTypes(materialTypeInfos);
        }

        return dto;
    }

    // Helper method to update existing entity with new DTO values
    public void updateEntity(ProductAttributeConsumableMedical entity,
                             ConsumableProductAttributeDTO dto) {
        if (dto == null || entity == null) return;

        entity.setBrandName(dto.getBrandName());
        entity.setSterileOrNonSterile(dto.getSterileOrNonSterile());
        entity.setDisposalOrReusable(dto.getDisposalOrReusable());
        entity.setShelfLife(dto.getShelfLife());
        entity.setDimensionSize(dto.getDimensionSize());
        entity.setPurpose(dto.getPurpose());
        entity.setKeyFeaturesSpecifications(dto.getKeyFeaturesSpecifications());
        entity.setSafetyInstructions(dto.getSafetyInstructions());
        entity.setManufacturerName(dto.getManufacturerName());
        entity.setBrochureType(dto.getBrochureType());
        entity.setBrochurePath(dto.getBrochurePath());

        // Update relationships
        if (dto.getDeviceCatId() != null) {
            DeviceCategory deviceCategory = deviceCategoryRepo.findById(dto.getDeviceCatId())
                    .orElseThrow(() -> new RuntimeException(
                            "DeviceCategory not found: " + dto.getDeviceCatId()));
            entity.setDeviceCategory(deviceCategory);
        }

        if (dto.getDeviceSubCatId() != null) {
            DeviceSubCategory deviceSubCategory = deviceSubCategoryRepo.findById(dto.getDeviceSubCatId())
                    .orElseThrow(() -> new RuntimeException(
                            "DeviceSubCategory not found: " + dto.getDeviceSubCatId()));
            entity.setDeviceSubCategory(deviceSubCategory);
        }

        if (dto.getCountryId() != null) {
            CountryMaster countryMaster = countryMasterRepo.findById(dto.getCountryId())
                    .orElseThrow(() -> new RuntimeException(
                            "Country not found: " + dto.getCountryId()));
            entity.setCountryMaster(countryMaster);
        }

        if (dto.getStorageConditionId() != null) {
            StorageConditionMaster storageConditionMaster = storageConditionMasterRepo.findById(dto.getStorageConditionId())
                    .orElseThrow(() -> new RuntimeException(
                            "StorageCondition not found: " + dto.getStorageConditionId()));
            entity.setStorageConditionMaster(storageConditionMaster);
        }

        // Update material types
        if (dto.getMaterialTypeId() != null) {
            List<ConsumableMaterialType> materialTypes = consumableMaterialTypeRepo.findAllById(dto.getMaterialTypeId());
            if (materialTypes.size() != dto.getMaterialTypeId().size()) {
                List<Long> foundIds = materialTypes.stream()
                        .map(ConsumableMaterialType::getMaterialTypeId)
                        .collect(Collectors.toList());
                List<Long> notFoundIds = dto.getMaterialTypeId().stream()
                        .filter(id -> !foundIds.contains(id))
                        .collect(Collectors.toList());
                throw new RuntimeException("MaterialTypes not found: " + notFoundIds);
            }
            entity.getMaterialTypes().clear();
            entity.getMaterialTypes().addAll(materialTypes);
        }
    }
}