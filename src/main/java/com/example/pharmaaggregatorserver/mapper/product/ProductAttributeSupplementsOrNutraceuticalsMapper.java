package com.example.pharmaaggregatorserver.mapper.product;

import com.example.pharmaaggregatorserver.dto.product.ProductAttributeSupplementsOrNutraceuticalsDto;
import com.example.pharmaaggregatorserver.dto.product.ProductCertificateDocumentDto;
import com.example.pharmaaggregatorserver.entity.product.MedicalDeviceProductMaster.Certification;
import com.example.pharmaaggregatorserver.entity.product.NetQuantityUnit;
import com.example.pharmaaggregatorserver.entity.product.ProductAttributeSupplementsOrNutraceuticals;
import com.example.pharmaaggregatorserver.entity.product.ProductCertificateDocument;
import com.example.pharmaaggregatorserver.entity.product.ServingSizeUnit;
import com.example.pharmaaggregatorserver.exception.NotFoundException;
import com.example.pharmaaggregatorserver.repository.product.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductAttributeSupplementsOrNutraceuticalsMapper {

    private final TherapeuticCategoryRepository therapeuticCategoryRepository;
    private final TherapeuticSubcategoryRepository therapeuticSubcategoryRepository;
    private final DosageFormRepository dosageFormRepository;
    private final AgeGroupMasterRepository ageGroupMasterRepository;
    private final FlavourRepository flavourRepository;
    private final StorageConditionMasterRepository storageConditionMasterRepository;
    private final CountryMasterRepository countryMasterRepository;
    private final CertificationRepository certificationRepository;
    private final NetQuantityUnitRepository netQuantityUnitRepository;
    private final ServingSizeUnitRepository servingSizeUnitRepository;

    // ================= DTO → ENTITY =================
    public ProductAttributeSupplementsOrNutraceuticals toEntity(ProductAttributeSupplementsOrNutraceuticalsDto dto) {
        if (dto == null) return null;

        ProductAttributeSupplementsOrNutraceuticals entity = new ProductAttributeSupplementsOrNutraceuticals();
        if (dto.getTherapeuticCategoryId() != null) {
            entity.setTherapeuticCategory(therapeuticCategoryRepository.findById(dto.getTherapeuticCategoryId())
                    .orElseThrow(() -> new RuntimeException(
                            "Therapeutic Category Not Found with id: " + dto.getTherapeuticCategoryId())));
        }

        if (dto.getTherapeuticSubCategoryId() != null) {
            entity.setTherapeuticSubCategory(therapeuticSubcategoryRepository.findById(dto.getTherapeuticSubCategoryId())
                    .orElseThrow(() -> new RuntimeException(
                            "Therapeutic Sub-Category Not Found with id: " + dto.getTherapeuticSubCategoryId())));
        }

        entity.setBrandName(dto.getBrandName());
        entity.setVariantName(dto.getVariantName());

        if (dto.getDosageFormId() != null) {
            entity.setDosageForm(dosageFormRepository.findById(dto.getDosageFormId())
                    .orElseThrow(() -> new RuntimeException(
                            "Dosage Form Not Found with id: " + dto.getDosageFormId())));
        }

        entity.setNetQuantity(dto.getNetQuantity());

        if (dto.getNetQuantityUnitId() != null) {
            NetQuantityUnit netQuantityUnit = netQuantityUnitRepository.findById(dto.getNetQuantityUnitId())
                    .orElseThrow(() -> new NotFoundException("Net Quantity Not found for Id: " + dto.getNetQuantityUnitId()));
            entity.setNetQuantityUnit(netQuantityUnit);
        }

        entity.setServingSize(dto.getServingSize());

        if (dto.getServingSizeUnitId() != null) {
            ServingSizeUnit servingSizeUnit = servingSizeUnitRepository.findById(dto.getServingSizeUnitId())
                    .orElseThrow(() -> new NotFoundException("Serving Size Unit Not Found for Id: " + dto.getServingSizeUnitId()));
            entity.setServingSizeUnit(servingSizeUnit);
        }

        entity.setStrength(dto.getStrength());
        entity.setActiveIngredients(dto.getActiveIngredients());
        entity.setOtherIngredients(dto.getOtherIngredients());
        entity.setNutritionalInformation(dto.getNutritionalInformation());

        if (dto.getNutritionalInformationImageUrl() != null) {
            entity.setNutritionalInformationImageUrl(dto.getNutritionalInformationImageUrl());
        }

        entity.setIntendedUse(dto.getIntendedUse());

        if (dto.getAgeGroupId() != null) {
            entity.setAgeGroupMaster(ageGroupMasterRepository.findById(dto.getAgeGroupId())
                    .orElseThrow(() -> new RuntimeException(
                            "Age Group Not Found with id: " + dto.getAgeGroupId())));
        }

        entity.setGender(dto.getGender());
        entity.setVegOrNonVegIndicator(dto.getVegOrNonVegIndicator());
        entity.setAllergenInformation(dto.getAllergenInformation());

        if (dto.getFlavourId() != null) {
            entity.setFlavour(flavourRepository.findById(dto.getFlavourId())
                    .orElseThrow(() -> new RuntimeException(
                            "Flavour Not Found with id: " + dto.getFlavourId())));
        }

        entity.setProductClaims(dto.getProductClaims());

        if (dto.getStorageConditionId() != null) {
            entity.setStorageConditionMaster(storageConditionMasterRepository.findById(dto.getStorageConditionId())
                    .orElseThrow(() -> new RuntimeException(
                            "StorageCondition not found: " + dto.getStorageConditionId())));
        }

//        entity.setManufacturerName(dto.getManufacturerName());

        if (dto.getCountryId() != null) {
            entity.setCountryMaster(countryMasterRepository.findById(dto.getCountryId())
                    .orElseThrow(() -> new RuntimeException(
                            "Country not found: " + dto.getCountryId())));
        }

        // Certificate Documents (ManyToMany certifications + OneToMany documents)
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
                        .supplementsOrNutraceuticals(entity)
                        .build();

                documents.add(doc);
            }

            entity.setCertifications(certifications);
            entity.setCertificateDocuments(documents);
        }

        entity.setBrochurePath(dto.getBrochurePath());
        return entity;
    }

    public ProductAttributeSupplementsOrNutraceuticalsDto toDto(ProductAttributeSupplementsOrNutraceuticals entity) {
        if (entity == null) return null;

        ProductAttributeSupplementsOrNutraceuticalsDto dto = new ProductAttributeSupplementsOrNutraceuticalsDto();
        dto.setProductAttributeId(entity.getProductAttributeId());

        if (entity.getTherapeuticCategory() != null) {
            dto.setTherapeuticCategoryId(entity.getTherapeuticCategory().getTherapeuticCategoryId());
            dto.setTherapeuticCategoryName(entity.getTherapeuticCategory().getTherapeuticCategory());
        }

        if (entity.getTherapeuticSubCategory() != null) {
            dto.setTherapeuticSubCategoryId(entity.getTherapeuticSubCategory().getTherapeuticSubcategoryId());
            dto.setTherapeuticSubCategoryName(entity.getTherapeuticSubCategory().getTherapeuticSubcategory());
        }

        dto.setBrandName(entity.getBrandName());
        dto.setVariantName(entity.getVariantName());

        if (entity.getDosageForm() != null) {
            dto.setDosageFormId(entity.getDosageForm().getDosageId());
            dto.setDosageFormName(entity.getDosageForm().getDosageName());
        }

        dto.setNetQuantity(entity.getNetQuantity());

        if (entity.getNetQuantityUnit() != null) {
            dto.setNetQuantityUnitId(entity.getNetQuantityUnit().getUnitId());
            dto.setNetQuantityUnitName(entity.getNetQuantityUnit().getUnitName());
        }

        dto.setServingSize(entity.getServingSize());

        if (entity.getServingSizeUnit() != null) {
            dto.setServingSizeUnitId(entity.getServingSizeUnit().getId());
            dto.setServingSizeUnitName(entity.getServingSizeUnit().getServingSizeUnit());
        }

        dto.setStrength(entity.getStrength());
        dto.setActiveIngredients(entity.getActiveIngredients());
        dto.setOtherIngredients(entity.getOtherIngredients());
        dto.setNutritionalInformation(entity.getNutritionalInformation());
        dto.setNutritionalInformationImageUrl(entity.getNutritionalInformationImageUrl());
        dto.setIntendedUse(entity.getIntendedUse());

        if (entity.getAgeGroupMaster() != null) {
            dto.setAgeGroupId(entity.getAgeGroupMaster().getAgeGroupId());
            dto.setAgeGroupName(entity.getAgeGroupMaster().getAgeGroup());
        }

        dto.setGender(entity.getGender());
        dto.setVegOrNonVegIndicator(entity.getVegOrNonVegIndicator());
        dto.setAllergenInformation(entity.getAllergenInformation());

        if (entity.getFlavour() != null) {
            dto.setFlavourId(entity.getFlavour().getFlavourId());
            dto.setFlavourName(entity.getFlavour().getFlavourName());
        }

        dto.setProductClaims(entity.getProductClaims());

        if (entity.getStorageConditionMaster() != null) {
            dto.setStorageConditionId(entity.getStorageConditionMaster().getStorageConditionId());
            dto.setStorageConditionName(entity.getStorageConditionMaster().getConditionName());
        }

//        dto.setManufacturerName(entity.getManufacturerName());

        if (entity.getCountryMaster() != null) {
            dto.setCountryId(entity.getCountryMaster().getCountryId());
            dto.setCountryName(entity.getCountryMaster().getCountryName());
        }

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

        dto.setBrochurePath(entity.getBrochurePath());

        return dto;
    }
}
