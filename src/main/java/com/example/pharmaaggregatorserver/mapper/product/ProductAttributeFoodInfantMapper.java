package com.example.pharmaaggregatorserver.mapper.product;

import com.example.pharmaaggregatorserver.dto.product.AgeGroupMasterDto;
import com.example.pharmaaggregatorserver.dto.product.ProductAttributeFoodInfantDto;
import com.example.pharmaaggregatorserver.dto.product.ProductCertificateDocumentDto;
import com.example.pharmaaggregatorserver.dto.product.ProductUserManualDto;
import com.example.pharmaaggregatorserver.entity.product.*;
import com.example.pharmaaggregatorserver.entity.product.MedicalDeviceProductMaster.Certification;
import com.example.pharmaaggregatorserver.exception.NotFoundException;
import com.example.pharmaaggregatorserver.repository.product.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductAttributeFoodInfantMapper {

    private final ProductCategoryMasterRepository productCategoryRepository;
    private final ProductSubcategoryMasterRepository productSubcategoryRepository;
    private final ProductFormMasterRepository productFormRepository;
    private final AgeGroupMasterRepository ageGroupRepository;
    private final StorageConditionMasterRepository storageConditionRepository;
    private final CountryMasterRepository countryRepository;
    private final CertificationRepository certificationRepository;
    private final AgeGroupMapper ageGroupMapper;
    private final NetQuantityUnitRepository netQuantityUnitRepository;
    private final ServingSizeUnitRepository servingSizeUnitRepository;


    // ===================== TO ENTITY =====================
    public ProductAttributeFoodInfant toEntity(ProductAttributeFoodInfantDto dto) {

        if (dto == null) return null;

        ProductAttributeFoodInfant entity = new ProductAttributeFoodInfant();

        entity.setProductAttributeId(dto.getProductAttributeId());
        entity.setBrandName(dto.getBrandName());
        entity.setVariantName(dto.getVariantName());
        entity.setNetQuantity(dto.getNetQuantity());
        entity.setServingSize(dto.getServingSize());
        entity.setVegNonvegIndicator(dto.getVegNonvegIndicator());
        entity.setAllergenInformation(dto.getAllergenInformation());
        entity.setNutritionalInformation(dto.getNutritionalInformation());
        entity.setNutritionalInformationImageUrl(dto.getNutritionalInformationImageUrl());
        entity.setActiveIngredients(dto.getActiveIngredients());
        entity.setAdditivesPreservatives(dto.getAdditivesPreservatives());
        entity.setProductClaims(dto.getProductClaims());
        entity.setProductUserManual(dto.getProductUserManual());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setModifiedBy(dto.getModifiedBy());
        entity.setCreatedDate(dto.getCreatedDate());
        entity.setModifiedDate(dto.getModifiedDate());

        entity.setProductCategoryMaster(
                productCategoryRepository.findById(dto.getProductCategoryId())
                        .orElseThrow(() -> new RuntimeException("Category not found"))
        );

        entity.setProductSubcategoryMaster(
                productSubcategoryRepository.findById(dto.getProductSubcategoryId())
                        .orElseThrow(() -> new RuntimeException("Subcategory not found"))
        );

        entity.setProductFormMaster(
                productFormRepository.findById(dto.getProductFormId())
                        .orElseThrow(() -> new RuntimeException("Form not found"))
        );

        entity.setNetQuantityUnit(
                netQuantityUnitRepository.findById(dto.getUnitId())
                        .orElseThrow(() -> new RuntimeException("Net Quantity Unit not found"))
        );

        if (dto.getServingSizeUnitId() != null) {
            ServingSizeUnit servingSizeUnit = servingSizeUnitRepository.findById(dto.getServingSizeUnitId())
                    .orElseThrow(() -> new NotFoundException("Serving Size Unit Not Found for Id: " + dto.getServingSizeUnitId()));
            entity.setServingSizeUnit(servingSizeUnit);
        }

//        if (dto.getAgeGroupMastersDto() != null &&
//                !dto.getAgeGroupMastersDto().isEmpty()) {
//
//            List<AgeGroupMaster> ageGroups =
//                    dto.getAgeGroupMastersDto()
//                            .stream()
//                            .map(ageGroupMapper::toEntity)
//                            .map(ageGroup ->
//                                    ageGroupRepository.findById(ageGroup.getAgeGroupId())
//                                            .orElseThrow(() ->
//                                                    new RuntimeException(
//                                                            "AgeGroup not found: "
//                                                                    + ageGroup.getAgeGroupId())))
//                            .toList();
//
//            entity.setAgeGroups(ageGroups);
//        }

        // ================= AGE GROUP =================

        if (dto.getAgeGroupIds() != null &&
                !dto.getAgeGroupIds().isEmpty()) {

            List<AgeGroupMaster> ageGroups =
                    ageGroupRepository.findAllById(dto.getAgeGroupIds());

            entity.setAgeGroups(ageGroups);

        } else if (dto.getAgeGroupMastersDto() != null &&
                !dto.getAgeGroupMastersDto().isEmpty()) {

            List<AgeGroupMaster> ageGroups =
                    dto.getAgeGroupMastersDto()
                            .stream()
                            .map(ageGroupMapper::toEntity)
                            .map(ageGroup ->
                                    ageGroupRepository.findById(ageGroup.getAgeGroupId())
                                            .orElseThrow(() ->
                                                    new RuntimeException(
                                                            "AgeGroup not found: "
                                                                    + ageGroup.getAgeGroupId())))
                            .toList();

            entity.setAgeGroups(ageGroups);
        }

        entity.setStorageConditionMaster(
                storageConditionRepository.findById(dto.getStorageConditionId())
                        .orElseThrow(() -> new RuntimeException("StorageCondition not found"))
        );

        entity.setCountryMaster(
                countryRepository.findById(dto.getCountryId())
                        .orElseThrow(() -> new RuntimeException("Country not found"))
        );


        if (dto.getCertificationIds() != null && !dto.getCertificationIds().isEmpty()) {

            List<Certification> certifications =
                    certificationRepository.findAllById(dto.getCertificationIds());

            entity.setCertifications(certifications);
        }

        if (dto.getCertificateDocuments() != null && !dto.getCertificateDocuments().isEmpty()) {
            List<Certification> certifications = new ArrayList<>();
            List<ProductCertificateDocument> documents = new ArrayList<>();

            for (ProductCertificateDocumentDto certDto : dto.getCertificateDocuments()) {
                Certification certification = certificationRepository.findById(certDto.getCertificationId())
                        .orElseThrow(() -> new RuntimeException(
                                "Certification not found: " + certDto.getCertificationId()));

                certifications.add(certification);

                ProductCertificateDocument doc = ProductCertificateDocument.builder()
                        .certification(certification)
                        .certificateUrl(certDto.getCertificateUrl())
                        .productAttributeFoodInfant(entity)
                        .build();

                documents.add(doc);
            }

            entity.setCertifications(certifications);
            entity.setCertificateDocuments(documents);
        }

        return entity;
    }

    // ===================== TO DTO =====================
    public ProductAttributeFoodInfantDto toDto(ProductAttributeFoodInfant entity) {

        if (entity == null) return null;

        ProductAttributeFoodInfantDto dto = new ProductAttributeFoodInfantDto();

        dto.setProductAttributeId(entity.getProductAttributeId());
        dto.setBrandName(entity.getBrandName());
        dto.setVariantName(entity.getVariantName());
        dto.setNetQuantity(entity.getNetQuantity());
        dto.setServingSize(entity.getServingSize());
        dto.setVegNonvegIndicator(entity.getVegNonvegIndicator());
        dto.setAllergenInformation(entity.getAllergenInformation());
        dto.setNutritionalInformation(entity.getNutritionalInformation());
        dto.setNutritionalInformationImageUrl(entity.getNutritionalInformationImageUrl());
        dto.setActiveIngredients(entity.getActiveIngredients());
        dto.setAdditivesPreservatives(entity.getAdditivesPreservatives());
        dto.setProductClaims(entity.getProductClaims());
        dto.setProductUserManual(entity.getProductUserManual());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setModifiedBy(entity.getModifiedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setModifiedDate(entity.getModifiedDate());

        // ================= RELATIONS =================

        if (entity.getProductCategoryMaster() != null) {
            dto.setProductCategoryId(entity.getProductCategoryMaster().getProductCategoryId());
        }

        if (entity.getProductSubcategoryMaster() != null) {
            dto.setProductSubcategoryId(entity.getProductSubcategoryMaster().getProductSubcategoryId());
        }

        if (entity.getProductFormMaster() != null) {
            dto.setProductFormId(entity.getProductFormMaster().getProductFormId());
        }

        if (entity.getNetQuantity() != null) {
            dto.setUnitId(entity.getNetQuantityUnit().getUnitId());
        }

        if (entity.getServingSizeUnit() != null) {
            dto.setServingSizeUnitId(entity.getServingSizeUnit().getId());
            dto.setServingSizeUnitName(entity.getServingSizeUnit().getServingSizeUnit());
        }

        if (entity.getAgeGroups() != null &&
                !entity.getAgeGroups().isEmpty()) {

            List<AgeGroupMasterDto> ageGroupDtos =
                    entity.getAgeGroups()
                            .stream()
                            .map(ageGroupMapper::toDto)
                            .toList();

            dto.setAgeGroupMastersDto(ageGroupDtos);
        }

        if (entity.getStorageConditionMaster() != null) {
            dto.setStorageConditionId(entity.getStorageConditionMaster().getStorageConditionId());
        }

        if (entity.getCountryMaster() != null) {
            dto.setCountryId(entity.getCountryMaster().getCountryId());
        }

        // ================= MANY TO MANY =================

        if (entity.getCertifications() != null) {
            List<Long> ids = entity.getCertifications()
                    .stream()
                    .map(Certification::getCertificationId)
                    .collect(Collectors.toList());

            dto.setCertificationIds(ids);
        }

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

        return dto;
    }
}
