package com.example.pharmaaggregatorserver.mapper.product;

import com.example.pharmaaggregatorserver.dto.product.ProductAttributeNonConsumableMedicalDto;
import com.example.pharmaaggregatorserver.dto.product.ProductCertificateDocumentDto;
import com.example.pharmaaggregatorserver.entity.product.MedicalDeviceProductMaster.Certification;
import com.example.pharmaaggregatorserver.entity.product.MedicalDeviceProductMaster.DeviceCategory;
import com.example.pharmaaggregatorserver.entity.product.MedicalDeviceProductMaster.NonConsumableMaterialType;
import com.example.pharmaaggregatorserver.entity.product.ProductAttributeNonConsumableMedical;
import com.example.pharmaaggregatorserver.entity.product.ProductCertificateDocument;
import com.example.pharmaaggregatorserver.repository.product.CertificationRepository;
import com.example.pharmaaggregatorserver.repository.product.DeviceCategoryRepository;
import com.example.pharmaaggregatorserver.repository.product.NonConsumableMaterialTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductAttributeNonConsumableMedicalMapper {

    private final DeviceCategoryRepository deviceCategoryRepo;
    private final CertificationRepository certificationRepo;
    private final NonConsumableMaterialTypeRepository materialTypeRepo;

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
        entity.setKeyFeaturesSpecifications(dto.getKeyFeaturesSpecifications());
//        entity.setComplianceCertificateUrl(dto.getComplianceCertificateUrl());
        entity.setWarrantyPeriod(dto.getWarrantyPeriod());
        entity.setServiceAvailability(dto.isServiceAvailability());
        entity.setCountryOfOrigin(dto.getCountryOfOrigin());
        entity.setManufacturerName(dto.getManufacturerName());
        entity.setStorageCondition(dto.getStorageCondition());
        entity.setBrochurePath(dto.getBrochurePath());

        if (dto.getDeviceCategoryId() != null) {
            DeviceCategory deviceCategory = deviceCategoryRepo.findById(dto.getDeviceCategoryId())
                    .orElseThrow(() -> new RuntimeException(
                            "DeviceCategory not found: " + dto.getDeviceCategoryId()));
            entity.setDeviceCategory(deviceCategory);
        }

//        if (dto.getCertificationId() != null) {
//            Certification certification = certificationRepo.findById(dto.getCertificationId())
//                    .orElseThrow(() -> new RuntimeException(
//                            "Certification not found: " + dto.getCertificationId()));
//            entity.setCertification(certification);
//        }

        if (dto.getMaterialTypeId() != null) {
            NonConsumableMaterialType materialType = materialTypeRepo.findById(dto.getMaterialTypeId())
                    .orElseThrow(() -> new RuntimeException(
                            "MaterialType not found: " + dto.getMaterialTypeId()));
            entity.setMaterialType(materialType);
        }

        // Map multiple certificate documents
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

        if (entity.getDeviceCategory() != null) {
            dto.setDeviceCategoryId(entity.getDeviceCategory().getDeviceCatId());
            dto.setDeviceName(entity.getDeviceCategory().getDeviceName());
        }

        dto.setBrandName(entity.getBrandName());
        dto.setModelName(entity.getModelName());
        dto.setModelNumber(entity.getModelNumber());
        dto.setDeviceClassification(entity.getDeviceClassification());
        dto.setUdiNumber(entity.getUdiNumber());
        dto.setPurpose(entity.getPurpose());
        dto.setKeyFeaturesSpecifications(entity.getKeyFeaturesSpecifications());

//        if (entity.getCertification() != null) {
//            dto.setCertificationId(entity.getCertification().getCertificationId());
//            dto.setCertificationName(entity.getCertification().getCertificationName());
//        }
//
//        dto.setComplianceCertificateUrl(entity.getComplianceCertificateUrl());

        // Map certificate documents back to DTOs
        if (entity.getCertificateDocuments() != null) {
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
                    .toList();
            dto.setCertificateDocuments(certDtos);
        }

        if (entity.getMaterialType() != null) {
            dto.setMaterialTypeId(entity.getMaterialType().getMaterialTypeId());
            dto.setMaterialTypeName(entity.getMaterialType().getMaterialTypeName());
        }

        dto.setWarrantyPeriod(entity.getWarrantyPeriod());
        dto.setServiceAvailability(entity.isServiceAvailability());
        dto.setCountryOfOrigin(entity.getCountryOfOrigin());
        dto.setManufacturerName(entity.getManufacturerName());
        dto.setStorageCondition(entity.getStorageCondition());
        dto.setBrochurePath(entity.getBrochurePath());
        return dto;
    }
}
