package com.example.pharmaaggregatorserver.mapper.product;

import com.example.pharmaaggregatorserver.dto.product.NonConsumableMaterialTypeDto;
import com.example.pharmaaggregatorserver.dto.product.ProductAttributeNonConsumableMedicalDto;
import com.example.pharmaaggregatorserver.dto.product.ProductCertificateDocumentDto;
import com.example.pharmaaggregatorserver.entity.product.MedicalDeviceProductMaster.*;
import com.example.pharmaaggregatorserver.entity.product.ProductAttributeNonConsumableMedical;
import com.example.pharmaaggregatorserver.entity.product.ProductCertificateDocument;
import com.example.pharmaaggregatorserver.exception.NotFoundException;
import com.example.pharmaaggregatorserver.repository.product.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductAttributeNonConsumableMedicalMapper {

    private final DeviceCategoryRepository deviceCategoryRepo;
    private final DeviceSubCategoryRepository deviceSubCategoryRepo;
    private final CertificationRepository certificationRepo;
    private final NonConsumableMaterialTypeRepository materialTypeRepo;
    private final CountryMasterRepository countryMasterRepository;
    private final StorageConditionMasterRepository storageConditionMasterRepository;
    private final PowerSourceMasterRepository powerSourceMasterRepository;
    private final DeviceSpecificationUnitRepository deviceSpecificationUnitRepo;

    public ProductAttributeNonConsumableMedical toEntity(ProductAttributeNonConsumableMedicalDto dto) {
        if (dto == null) return null;

        ProductAttributeNonConsumableMedical entity = new ProductAttributeNonConsumableMedical();
        entity.setProductAttributeId(dto.getProductAttributeId());
        entity.setBrandName(dto.getBrandName());
        entity.setModelName(dto.getModelName());
        entity.setModelNumber(dto.getModelNumber());
        entity.setDeviceClassification(dto.getDeviceClassification());
        entity.setUdiNumber(dto.getUdiNumber());
        entity.setPurpose(dto.getPurpose());
        entity.setDimensionSize(dto.getDimensionSize());

        if (dto.getDeviceSpecificationUnitId() != null) {
            DeviceSpecificationUnit deviceSpecificationUnit = deviceSpecificationUnitRepo.findById(dto.getDeviceSpecificationUnitId())
                    .orElseThrow(() -> new NotFoundException("Device Specification Unit Not Found with Id: " + dto.getDeviceSpecificationUnitId()));
            entity.setDeviceSpecificationUnit(deviceSpecificationUnit);
        }

        entity.setKeyFeaturesSpecifications(dto.getKeyFeaturesSpecifications());
        entity.setWarrantyPeriod(dto.getWarrantyPeriod());
        entity.setServiceAvailability(dto.isServiceAvailability());
        entity.setManufacturerName(dto.getManufacturerName());
        entity.setBrochurePath(dto.getBrochurePath());

        // Device Category
        if (dto.getDeviceCategoryId() != null) {
            DeviceCategory deviceCategory = deviceCategoryRepo.findById(dto.getDeviceCategoryId())
                    .orElseThrow(() -> new NotFoundException(
                            "DeviceCategory not found: " + dto.getDeviceCategoryId()));
            entity.setDeviceCategory(deviceCategory);
        }

        // Device Sub-Category
        if (dto.getDeviceSubCategoryId() != null) {
            DeviceSubCategory deviceSubCategory = deviceSubCategoryRepo.findById(dto.getDeviceSubCategoryId())
                    .orElseThrow(() -> new NotFoundException(
                            "DeviceSubCategory not found: " + dto.getDeviceSubCategoryId()));
            entity.setDeviceSubCategory(deviceSubCategory);
        }

        // Material Types (ManyToMany list)
        if (dto.getMaterialTypeIds() != null && !dto.getMaterialTypeIds().isEmpty()) {
            List<NonConsumableMaterialType> materialTypes = new ArrayList<>();
            for (Long materialTypeId : dto.getMaterialTypeIds()) {
                NonConsumableMaterialType materialType = materialTypeRepo.findById(materialTypeId)
                        .orElseThrow(() -> new NotFoundException(
                                "MaterialType not found: " + materialTypeId));
                materialTypes.add(materialType);
            }
            entity.setMaterialTypes(materialTypes);
        }

        // Power Source
        if (dto.getPowerSourceId() != null) {
            entity.setPowerSource(powerSourceMasterRepository.findById(dto.getPowerSourceId())
                    .orElseThrow(() -> new NotFoundException(
                            "PowerSource not found: " + dto.getPowerSourceId())));
        }

        // Country of Origin
        if (dto.getCountryId() != null) {
            entity.setCountryMaster(countryMasterRepository.findById(dto.getCountryId())
                    .orElseThrow(() -> new NotFoundException(
                            "Country not found: " + dto.getCountryId())));
        }

        // Storage Condition
        if (dto.getStorageConditionId() != null) {
            entity.setStorageConditionMaster(storageConditionMasterRepository.findById(dto.getStorageConditionId())
                    .orElseThrow(() -> new NotFoundException(
                            "StorageCondition not found: " + dto.getStorageConditionId())));
        }

        // Certificate Documents (ManyToMany certifications + OneToMany documents)
        if (dto.getCertificateDocuments() != null && !dto.getCertificateDocuments().isEmpty()) {
            List<Certification> certifications = new ArrayList<>();
            List<ProductCertificateDocument> documents = new ArrayList<>();

            for (ProductCertificateDocumentDto certDto : dto.getCertificateDocuments()) {
                Certification certification = certificationRepo.findById(certDto.getCertificationId())
                        .orElseThrow(() -> new NotFoundException(
                                "Certification not found: " + certDto.getCertificationId()));

                certifications.add(certification);

                ProductCertificateDocument doc = ProductCertificateDocument.builder()
                        .certification(certification)
                        .certificateUrl(certDto.getCertificateUrl())
                        .nonConsumableMedical(entity)
                        .build();

                documents.add(doc);
            }

            entity.setCertifications(certifications);
            entity.setCertificateDocuments(documents);
        }

        return entity;
    }

    public ProductAttributeNonConsumableMedicalDto toDto(ProductAttributeNonConsumableMedical entity) {
        if (entity == null) return null;

        ProductAttributeNonConsumableMedicalDto dto = new ProductAttributeNonConsumableMedicalDto();
        dto.setProductAttributeId(entity.getProductAttributeId());

        // Device Category
        if (entity.getDeviceCategory() != null) {
            dto.setDeviceCategoryId(entity.getDeviceCategory().getDeviceCatId());
            dto.setDeviceName(entity.getDeviceCategory().getDeviceName());
        }

        // Device Sub-Category
        if (entity.getDeviceSubCategory() != null) {
            dto.setDeviceSubCategoryId(entity.getDeviceSubCategory().getDeviceSubCatId());
            dto.setDeviceSubCategoryName(entity.getDeviceSubCategory().getSubCategoryName());
        }

        dto.setBrandName(entity.getBrandName());
        dto.setModelName(entity.getModelName());
        dto.setModelNumber(entity.getModelNumber());
        dto.setDeviceClassification(entity.getDeviceClassification());
        dto.setUdiNumber(entity.getUdiNumber());
        dto.setPurpose(entity.getPurpose());
        dto.setDimensionSize(entity.getDimensionSize());

        if (entity.getDeviceSpecificationUnit() != null) {
            dto.setDeviceSpecificationUnitId(entity.getDeviceSpecificationUnit().getUnitId());
            dto.setDeviceSpecificationUnitName(entity.getDeviceSpecificationUnit().getUnitName());
        }

        dto.setKeyFeaturesSpecifications(entity.getKeyFeaturesSpecifications());

        // Certificate Documents
        if (entity.getCertificateDocuments() != null) {
            List<ProductCertificateDocumentDto> certDtos = entity.getCertificateDocuments().stream()
                    .map(doc -> {
                        ProductCertificateDocumentDto certDto = new ProductCertificateDocumentDto();
                        certDto.setProductCertificateDocumentId(doc.getProductCertificateDocumentId());
                        if (doc.getCertification() != null) {
                            certDto.setCertificationId(doc.getCertification().getCertificationId());
                            certDto.setCertificationName(doc.getCertification().getCertificationName());
                        }
                        certDto.setCertificateUrl(doc.getCertificateUrl());
                        return certDto;
                    })
                    .toList();
            dto.setCertificateDocuments(certDtos);
        }

        // Material Types (ManyToMany list → List<NonConsumableMaterialTypeDto>)
        if (entity.getMaterialTypes() != null) {
            List<Long> materialTypeIds = new ArrayList<>();
            List<NonConsumableMaterialTypeDto> materialTypeDtos = new ArrayList<>();

            for (NonConsumableMaterialType materialType : entity.getMaterialTypes()) {
                materialTypeIds.add(materialType.getMaterialTypeId());

                NonConsumableMaterialTypeDto materialTypeDto = new NonConsumableMaterialTypeDto();
                materialTypeDto.setMaterialTypeId(materialType.getMaterialTypeId());
                materialTypeDto.setMaterialTypeName(materialType.getMaterialTypeName());
                materialTypeDtos.add(materialTypeDto);
            }

            dto.setMaterialTypeIds(materialTypeIds);
            dto.setMaterialTypes(materialTypeDtos);
        }

        // Power Source
        if (entity.getPowerSource() != null) {
            dto.setPowerSourceId(entity.getPowerSource().getPowerSourceId());
            dto.setPowerSourceName(entity.getPowerSource().getPowerSourceName());
        }

        dto.setWarrantyPeriod(entity.getWarrantyPeriod());
        dto.setServiceAvailability(entity.isServiceAvailability());

        // Country of Origin
        if (entity.getCountryMaster() != null) {
            dto.setCountryId(entity.getCountryMaster().getCountryId());
            dto.setCountryName(entity.getCountryMaster().getCountryName());
        }

        dto.setManufacturerName(entity.getManufacturerName());

        // Storage Condition
        if (entity.getStorageConditionMaster() != null) {
            dto.setStorageConditionId(entity.getStorageConditionMaster().getStorageConditionId());
            dto.setStorageConditionName(entity.getStorageConditionMaster().getConditionName());
        }

        dto.setBrochurePath(entity.getBrochurePath());

        return dto;
    }
}
