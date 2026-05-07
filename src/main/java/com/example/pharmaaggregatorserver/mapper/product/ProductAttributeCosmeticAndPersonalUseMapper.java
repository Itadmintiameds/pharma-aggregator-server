package com.example.pharmaaggregatorserver.mapper.product;

import com.example.pharmaaggregatorserver.dto.product.CosmeticAndPersonalUseProductAttributeDTO;
import com.example.pharmaaggregatorserver.dto.product.ProductCertificateDocumentDto;
import com.example.pharmaaggregatorserver.entity.product.ProductAttributeCosmeticandPersonalCare;
import com.example.pharmaaggregatorserver.entity.product.ProductCertificateDocument;
import com.example.pharmaaggregatorserver.entity.product.MedicalDeviceProductMaster.*;
import com.example.pharmaaggregatorserver.entity.product.CosmeticPersonalCareMasters.*;
import com.example.pharmaaggregatorserver.repository.product.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductAttributeCosmeticAndPersonalUseMapper {
    private final ProductCategoryMasterRepository productCategoryMasterRepository;
    private final ProductSubcategoryMasterRepository productSubCategoryMasterRepository;
    private final intendedUseAreaRepository intendedUseAreaRepository;
    private final skinTypeRepository skinTypeRepository;
    private final hairTypeRepository hairTypeRepository;
    private final AgeGroupMasterRepository ageGroupMasterRepository;
    private final CertificationRepository certificationRepo;
    private final CountryMasterRepository countryMasterRepo;
    private final StorageConditionMasterRepository storageConditionMasterRepo;

    // ================= DTO → ENTITY =================
    public ProductAttributeCosmeticandPersonalCare toEntity(CosmeticAndPersonalUseProductAttributeDTO dto) {
        if (dto == null) return null;

        ProductAttributeCosmeticandPersonalCare entity = new ProductAttributeCosmeticandPersonalCare();

        // Basic fields - Fixed the incomplete lines
        entity.setProductAttributeId(dto.getProductAttributeId());
        entity.setBrandName(dto.getBrandName());
        entity.setVariantName(dto.getVariantName());
        entity.setGender(dto.getGender());
        entity.setActiveIngredients(dto.getActiveIngredients());
        entity.setNetQuantityStrength(dto.getNetQuantityStrength());
        entity.setProductClaims(dto.getProductClaims());
        entity.setManufacturerName(dto.getManufacturerName());
        entity.setBrochurePath(dto.getBrochurePath());

        // Remove these fields as they don't exist in your entity/DTO
        // entity.setSterileOrNonSterile(dto.getSterileOrNonSterile());
        // entity.setDisposalOrReusable(dto.getDisposalOrReusable());
        // entity.setShelfLife(dto.getShelfLife());
        // entity.setDimensionSize(dto.getDimensionSize());
        // entity.setPurpose(dto.getPurpose());
        // entity.setKeyFeaturesSpecifications(dto.getKeyFeaturesSpecifications());
        // entity.setSafetyInstructions(dto.getSafetyInstructions());
        // entity.setBrochureType(dto.getBrochureType());

        // Set Product Category
        if (dto.getProductCategoryId() != null) {
            productCategoryMasterRepository.findById(dto.getProductCategoryId())
                    .ifPresent(entity::setProductCategoryMaster);
        }

        // Set Product Subcategory
        if (dto.getProductSubcategoryId() != null) {
            productSubCategoryMasterRepository.findById(dto.getProductSubcategoryId())
                    .ifPresent(entity::setProductSubcategoryMaster);
        }

        // Set Intended Use Areas (Many-to-Many)
        if (dto.getUseAreaId() != null && !dto.getUseAreaId().isEmpty()) {
            List<IntendedUseArea> useAreas = intendedUseAreaRepository.findAllById(dto.getUseAreaId());
            entity.setIntendedUseArea(useAreas);
        }

        // Set Skin Types (Many-to-Many)
        if (dto.getSkintypeId() != null && !dto.getSkintypeId().isEmpty()) {
            List<SkinType> skinTypes = skinTypeRepository.findAllById(dto.getSkintypeId());
            entity.setSkinType(skinTypes);
        }

        // Set Hair Types (Many-to-Many)
        if (dto.getTypeId() != null && !dto.getTypeId().isEmpty()) {
            List<HairType> hairTypes = hairTypeRepository.findAllById(dto.getTypeId());
            entity.setHairTypes(hairTypes);
        }

        // Set Age Group
        if (dto.getAgeGroupId() != null) {
            ageGroupMasterRepository.findById(dto.getAgeGroupId())
                    .ifPresent(entity::setAgeGroupMaster);
        }

        // Set Storage Condition
        if (dto.getStorageConditionId() != null) {
            storageConditionMasterRepo.findById(dto.getStorageConditionId())
                    .ifPresent(entity::setStorageConditionMaster);
        }

        // Set Country
        if (dto.getCountryId() != null) {
            countryMasterRepo.findById(dto.getCountryId())
                    .ifPresent(entity::setCountryMaster);
        }

        // Set Certificate Documents
        if (dto.getCertificateDocuments() != null && !dto.getCertificateDocuments().isEmpty()) {
            List<ProductCertificateDocument> documents = new ArrayList<>();
            List<Certification> certifications = new ArrayList<>();

            for (ProductCertificateDocumentDto certDto : dto.getCertificateDocuments()) {
                if (certDto.getCertificationId() != null) {
                    certificationRepo.findById(certDto.getCertificationId()).ifPresent(certification -> {
                        certifications.add(certification);

                        ProductCertificateDocument doc = ProductCertificateDocument.builder()
                                .certification(certification)
                                .certificateUrl(certDto.getCertificateUrl())
                                .cosmeticAndPersonalUse(entity)
                                .build();
                        documents.add(doc);
                    });
                }
            }

            entity.setCertifications(certifications);
            entity.setCertificateDocuments(documents);
        }

        return entity;
    }

    // ================= ENTITY → DTO =================
    public CosmeticAndPersonalUseProductAttributeDTO toDto(ProductAttributeCosmeticandPersonalCare entity) {
        if (entity == null) return null;

        CosmeticAndPersonalUseProductAttributeDTO dto = new CosmeticAndPersonalUseProductAttributeDTO();

        // Basic fields
        dto.setProductAttributeId(entity.getProductAttributeId());
        dto.setBrandName(entity.getBrandName());
        dto.setVariantName(entity.getVariantName());
        dto.setGender(entity.getGender());
        dto.setActiveIngredients(entity.getActiveIngredients());
        dto.setNetQuantityStrength(entity.getNetQuantityStrength());
        dto.setProductClaims(entity.getProductClaims());
        dto.setManufacturerName(entity.getManufacturerName());
        dto.setBrochurePath(entity.getBrochurePath());

        // Product Category
        if (entity.getProductCategoryMaster() != null) {
            dto.setProductCategoryId(entity.getProductCategoryMaster().getProductCategoryId());
        }

        // Product Subcategory
        if (entity.getProductSubcategoryMaster() != null) {
            dto.setProductSubcategoryId(entity.getProductSubcategoryMaster().getProductSubcategoryId());
        }

        // Intended Use Areas
        if (entity.getIntendedUseArea() != null && !entity.getIntendedUseArea().isEmpty()) {
            List<Long> useAreaIds = entity.getIntendedUseArea().stream()
                    .map(IntendedUseArea::getUseAreaId)
                    .collect(Collectors.toList());
            dto.setUseAreaId(useAreaIds);

            List<CosmeticAndPersonalUseProductAttributeDTO.IntendedAreaInfo> areaInfos =
                    entity.getIntendedUseArea().stream()
                            .map(area -> {
                                CosmeticAndPersonalUseProductAttributeDTO.IntendedAreaInfo info =
                                        new CosmeticAndPersonalUseProductAttributeDTO.IntendedAreaInfo();
                                info.setUseAreaId(area.getUseAreaId());
                                info.setAreaName(area.getAreaName());
                                return info;
                            })
                            .collect(Collectors.toList());
            dto.setIntendedarea(areaInfos);
        }

        // Skin Types
        if (entity.getSkinType() != null && !entity.getSkinType().isEmpty()) {
            List<Long> skinTypeIds = entity.getSkinType().stream()
                    .map(SkinType::getSkintypeId)
                    .collect(Collectors.toList());
            dto.setSkintypeId(skinTypeIds);

            List<CosmeticAndPersonalUseProductAttributeDTO.SkinTypeInfo> skinTypeInfos =
                    entity.getSkinType().stream()
                            .map(skin -> {
                                CosmeticAndPersonalUseProductAttributeDTO.SkinTypeInfo info =
                                        new CosmeticAndPersonalUseProductAttributeDTO.SkinTypeInfo();
                                info.setSkintypeId(skin.getSkintypeId());
                                info.setTypeName(skin.getTypeName());
                                return info;
                            })
                            .collect(Collectors.toList());
            dto.setSkinType(skinTypeInfos);
        }

        // Hair Types
        if (entity.getHairTypes() != null && !entity.getHairTypes().isEmpty()) {
            List<Long> hairTypeIds = entity.getHairTypes().stream()
                    .map(HairType::getTypeId)
                    .collect(Collectors.toList());
            dto.setTypeId(hairTypeIds);

            List<CosmeticAndPersonalUseProductAttributeDTO.HairTypeInfo> hairTypeInfos =
                    entity.getHairTypes().stream()
                            .map(hair -> {
                                CosmeticAndPersonalUseProductAttributeDTO.HairTypeInfo info =
                                        new CosmeticAndPersonalUseProductAttributeDTO.HairTypeInfo();
                                info.setTypeId(hair.getTypeId());
                                info.setTypeName(hair.getTypeName());
                                return info;
                            })
                            .collect(Collectors.toList());
            dto.setHairType(hairTypeInfos);
        }

        // Age Group
        if (entity.getAgeGroupMaster() != null) {
            dto.setAgeGroupId(entity.getAgeGroupMaster().getAgeGroupId());
        }

        // Storage Condition
        if (entity.getStorageConditionMaster() != null) {
            dto.setStorageConditionId(entity.getStorageConditionMaster().getStorageConditionId());
        }

        // Country
        if (entity.getCountryMaster() != null) {
            dto.setCountryId(entity.getCountryMaster().getCountryId());
        }

        // Certificate Documents
        if (entity.getCertificateDocuments() != null && !entity.getCertificateDocuments().isEmpty()) {
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
                    .collect(Collectors.toList());
            dto.setCertificateDocuments(certDtos);
        }

        return dto;
    }

    // Helper method to update existing entity with new DTO values
    public void updateEntity(ProductAttributeCosmeticandPersonalCare entity,
                             CosmeticAndPersonalUseProductAttributeDTO dto) {
        if (dto == null || entity == null) return;

        // Update basic fields
        entity.setBrandName(dto.getBrandName());
        entity.setVariantName(dto.getVariantName());
        entity.setGender(dto.getGender());
        entity.setActiveIngredients(dto.getActiveIngredients());
        entity.setNetQuantityStrength(dto.getNetQuantityStrength());
        entity.setProductClaims(dto.getProductClaims());
        entity.setManufacturerName(dto.getManufacturerName());
        entity.setBrochurePath(dto.getBrochurePath());

        // Update relationships (simplified - you may need more logic here)
        if (dto.getProductCategoryId() != null) {
            productCategoryMasterRepository.findById(dto.getProductCategoryId())
                    .ifPresent(entity::setProductCategoryMaster);
        }

        if (dto.getProductSubcategoryId() != null) {
            productSubCategoryMasterRepository.findById(dto.getProductSubcategoryId())
                    .ifPresent(entity::setProductSubcategoryMaster);
        }

        if (dto.getUseAreaId() != null && !dto.getUseAreaId().isEmpty()) {
            List<IntendedUseArea> useAreas = intendedUseAreaRepository.findAllById(dto.getUseAreaId());
            entity.getIntendedUseArea().clear();
            entity.getIntendedUseArea().addAll(useAreas);
        }

        if (dto.getSkintypeId() != null && !dto.getSkintypeId().isEmpty()) {
            List<SkinType> skinTypes = skinTypeRepository.findAllById(dto.getSkintypeId());
            entity.getSkinType().clear();
            entity.getSkinType().addAll(skinTypes);
        }

        if (dto.getTypeId() != null && !dto.getTypeId().isEmpty()) {
            List<HairType> hairTypes = hairTypeRepository.findAllById(dto.getTypeId());
            entity.getHairTypes().clear();
            entity.getHairTypes().addAll(hairTypes);
        }

        if (dto.getAgeGroupId() != null) {
            ageGroupMasterRepository.findById(dto.getAgeGroupId())
                    .ifPresent(entity::setAgeGroupMaster);
        }

        if (dto.getStorageConditionId() != null) {
            storageConditionMasterRepo.findById(dto.getStorageConditionId())
                    .ifPresent(entity::setStorageConditionMaster);
        }

        if (dto.getCountryId() != null) {
            countryMasterRepo.findById(dto.getCountryId())
                    .ifPresent(entity::setCountryMaster);
        }
    }
}