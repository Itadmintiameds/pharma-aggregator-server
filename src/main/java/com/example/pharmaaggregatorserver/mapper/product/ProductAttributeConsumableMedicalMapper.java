package com.example.pharmaaggregatorserver.mapper.product;

import com.example.pharmaaggregatorserver.dto.product.ConsumableProductAttributeDTO;
import com.example.pharmaaggregatorserver.entity.product.MedicalDeviceProductMaster.Certification;
import com.example.pharmaaggregatorserver.entity.product.MedicalDeviceProductMaster.ConsumableMaterialType;
import com.example.pharmaaggregatorserver.entity.product.MedicalDeviceProductMaster.DeviceCategory;
import com.example.pharmaaggregatorserver.entity.product.MedicalDeviceProductMaster.DimensionSize;
import com.example.pharmaaggregatorserver.entity.product.ProductAttributeConsumableMedical;
import com.example.pharmaaggregatorserver.repository.product.CertificationRepository;
import com.example.pharmaaggregatorserver.repository.product.DeviceCategoryRepository;
import com.example.pharmaaggregatorserver.repository.product.MaterialTypeRepository;
import com.example.pharmaaggregatorserver.repository.product.DimensionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductAttributeConsumableMedicalMapper {

    private final DeviceCategoryRepository deviceCategoryRepo;
    private final CertificationRepository certificationRepo;
    private final MaterialTypeRepository materialTypeRepo;
    private final DimensionRepository dimensionRepo;

    // ================= DTO → ENTITY =================
    public ProductAttributeConsumableMedical toEntity(ConsumableProductAttributeDTO dto) {
        if (dto == null) return null;

        ProductAttributeConsumableMedical entity = new ProductAttributeConsumableMedical();

        entity.setProductAttributeId(dto.getProductAttributeId());
        entity.setBrandName(dto.getBrandName());
        entity.setSterileOrNonSterile(dto.getSterileOrNonSterile());
        entity.setDisposalOrReusable(dto.getDisposalOrReusable());
        entity.setShelfLife(dto.getShelfLife());
        entity.setPurpose(dto.getPurpose());
        entity.setKeyFeaturesSpecifications(dto.getKeyFeaturesSpecifications());
        entity.setCountryOfOrigin(dto.getCountryOfOrigin());
        entity.setManufacturerName(dto.getManufacturerName());
        entity.setStorageCondition(dto.getStorageCondition());
        entity.setBrochureType(dto.getBrochureType());
        entity.setBrochurePath(dto.getBrochurePath());

        // Device Category
        if (dto.getDeviceCatId() != null) {
            DeviceCategory deviceCategory = deviceCategoryRepo.findById(dto.getDeviceCatId())
                    .orElseThrow(() -> new RuntimeException(
                            "DeviceCategory not found: " + dto.getDeviceCatId()));
            entity.setDeviceCategory(deviceCategory);
        }

        // Certification (SINGLE - as per your DTO)
        if (dto.getCertificationId() != null) {
            Certification certification = certificationRepo.findById(dto.getCertificationId())
                    .orElseThrow(() -> new RuntimeException(
                            "Certification not found: " + dto.getCertificationId()));
            entity.setCertification(certification);
        }

        // Material Type
        if (dto.getMaterialTypeId() != null) {
            ConsumableMaterialType materialType = materialTypeRepo.findById(dto.getMaterialTypeId())
                    .orElseThrow(() -> new RuntimeException(
                            "MaterialType not found: " + dto.getMaterialTypeId()));
            entity.setMaterialType(materialType);
        }

        // Dimension
        if (dto.getDiamensionId() != null) {
            DimensionSize dimension = dimensionRepo.findById(dto.getDiamensionId())
                    .orElseThrow(() -> new RuntimeException(
                            "Dimension not found: " + dto.getDiamensionId()));
            entity.setDimensionSize(dimension);
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
        dto.setPurpose(entity.getPurpose());
        dto.setKeyFeaturesSpecifications(entity.getKeyFeaturesSpecifications());
        dto.setCountryOfOrigin(entity.getCountryOfOrigin());
        dto.setManufacturerName(entity.getManufacturerName());
        dto.setStorageCondition(entity.getStorageCondition());
        dto.setBrochureType(entity.getBrochureType());
        dto.setBrochurePath(entity.getBrochurePath());

        // Device Category
        if (entity.getDeviceCategory() != null) {
            dto.setDeviceCatId(entity.getDeviceCategory().getDeviceCatId());
        }

        // Certification
        if (entity.getCertification() != null) {
            dto.setCertificationId(entity.getCertification().getCertificationId());
            dto.setCertificationName(entity.getCertification().getCertificationName());
        }

        // Material Type
        if (entity.getMaterialType() != null) {
            dto.setMaterialTypeId(entity.getMaterialType().getMaterialTypeId());
        }

        // Dimension
        if (entity.getDimensionSize() != null) {
            dto.setDiamensionId(entity.getDimensionSize().getDimensionId());
        }

        return dto;
    }
}