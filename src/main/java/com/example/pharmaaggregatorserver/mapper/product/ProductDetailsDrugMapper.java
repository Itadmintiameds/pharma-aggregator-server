package com.example.pharmaaggregatorserver.mapper.product;

import com.example.pharmaaggregatorserver.dto.product.*;
import com.example.pharmaaggregatorserver.entity.product.*;

import java.util.Set;
import java.util.stream.Collectors;

public class ProductDetailsDrugMapper {

    // Entity → Dto conversion
    public static ProductDetailsDrugDto toDto(ProductDetailsDrug entity) {
        if (entity == null) return null;

        ProductDetailsDrugDto dto = new ProductDetailsDrugDto();

        dto.setProductId(entity.getProductId());
        dto.setProductCategoryId(entity.getProductCategoryId());
        dto.setProductName(entity.getProductName());
        dto.setTherapeuticCategory(entity.getTherapeuticCategory());
        dto.setTherapeuticSubcategory(entity.getTherapeuticSubcategory());
        dto.setDosageForm(entity.getDosageForm());
        dto.setStrength(entity.getStrength());
        dto.setWarningsPrecautions(entity.getWarningsPrecautions());
        dto.setProductDescription(entity.getProductDescription());
        dto.setProductImage(entity.getProductImage());
        dto.setProductMarketingUrl(entity.getProductMarketingUrl());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setModifiedBy(entity.getModifiedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setModifiedDate(entity.getModifiedDate());

            if (entity.getMolecules() != null) {
            dto.setMolecules(
                    entity.getMolecules().stream()
                            .map(ProductDetailsDrugMapper::toMoleculeDto)
                            .collect(Collectors.toSet())
            );
        }


        dto.setPackagingDetails(toPackagingDto(entity.getPackagingDetails()));

        if (entity.getPricingDetails() != null) {
            dto.setPricingDetails(
                    entity.getPricingDetails().stream()
                            .map(ProductDetailsDrugMapper::toPricingDto)
                            .collect(Collectors.toSet())
            );
        }

        return dto;
    }

    // Dto → Entity conversion
    public static ProductDetailsDrug toEntity(ProductDetailsDrugDto dto) {
        if (dto == null) return null;

        ProductDetailsDrug entity = new ProductDetailsDrug();

        entity.setProductId(dto.getProductId());
        entity.setProductCategoryId(dto.getProductCategoryId());
        entity.setProductName(dto.getProductName());
        entity.setTherapeuticCategory(dto.getTherapeuticCategory());
        entity.setTherapeuticSubcategory(dto.getTherapeuticSubcategory());
        entity.setDosageForm(dto.getDosageForm());
        entity.setStrength(dto.getStrength());
        entity.setWarningsPrecautions(dto.getWarningsPrecautions());
        entity.setProductDescription(dto.getProductDescription());
        entity.setProductImage(dto.getProductImage());
        entity.setProductMarketingUrl(dto.getProductMarketingUrl());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setModifiedBy(dto.getModifiedBy());
        entity.setCreatedDate(dto.getCreatedDate());
        entity.setModifiedDate(dto.getModifiedDate());

        if (dto.getMolecules() != null) {
            entity.setMolecules(
                    dto.getMolecules().stream()
                            .map(ProductDetailsDrugMapper::toMoleculeEntity)
                            .collect(Collectors.toSet())
            );
        }

        PackagingDetailsDrug packaging = toPackagingEntity(dto.getPackagingDetails());
        if (packaging != null) {
            packaging.setProduct(entity);
            entity.setPackagingDetails(packaging);
        }

        if (dto.getPricingDetails() != null) {
            Set<PricingDetailsDrug> pricingEntities =
                    dto.getPricingDetails().stream()
                            .map(ProductDetailsDrugMapper::toPricingEntity)
                            .collect(Collectors.toSet());

            pricingEntities.forEach(p -> p.setProduct(entity));
            entity.setPricingDetails(pricingEntities);
        }

        return entity;
    }

    // Molecule Mapping
    public static MoleculeDto toMoleculeDto(Molecule entity) {
        MoleculeDto dto = new MoleculeDto();
        dto.setMoleculeId(entity.getMoleculeId());
        dto.setMoleculeName(entity.getMoleculeName());
        dto.setMechanismOfAction(entity.getMechanismOfAction());
        dto.setPrimaryUse(entity.getPrimaryUse());
        return dto;
    }

    public static Molecule toMoleculeEntity(MoleculeDto dto) {
        Molecule entity = new Molecule();
        entity.setMoleculeId(dto.getMoleculeId());
        entity.setMoleculeName(dto.getMoleculeName());
        entity.setMechanismOfAction(dto.getMechanismOfAction());
        entity.setPrimaryUse(dto.getPrimaryUse());
        return entity;
    }

    // Packaging Mapping
    public static PackagingDetailsDrugDto toPackagingDto(PackagingDetailsDrug entity) {
        if (entity == null) return null;

        PackagingDetailsDrugDto dto = new PackagingDetailsDrugDto();
        dto.setPackagingId(entity.getPackagingId());
        dto.setPackagingUnit(entity.getPackagingUnit());
        dto.setNumberOfUnits(entity.getNumberOfUnits());
        dto.setPackSize(entity.getPackSize());
        dto.setMinimumOrderQuantity(entity.getMinimumOrderQuantity());
        dto.setMaximumOrderQuantity(entity.getMaximumOrderQuantity());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setModifiedBy(entity.getModifiedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setModifiedDate(entity.getModifiedDate());

        return dto;
    }

    public static PackagingDetailsDrug toPackagingEntity(PackagingDetailsDrugDto dto) {
        if (dto == null) return null;

        PackagingDetailsDrug entity = new PackagingDetailsDrug();
        entity.setPackagingId(dto.getPackagingId());
        entity.setPackagingUnit(dto.getPackagingUnit());
        entity.setNumberOfUnits(dto.getNumberOfUnits());
        entity.setPackSize(dto.getPackSize());
        entity.setMinimumOrderQuantity(dto.getMinimumOrderQuantity());
        entity.setMaximumOrderQuantity(dto.getMaximumOrderQuantity());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setModifiedBy(dto.getModifiedBy());
        entity.setCreatedDate(dto.getCreatedDate());
        entity.setModifiedDate(dto.getModifiedDate());

        return entity;
    }

    // Pricing Mapping
    public static PricingDetailsDrugDto toPricingDto(PricingDetailsDrug entity) {
        PricingDetailsDrugDto dto = new PricingDetailsDrugDto();
        dto.setPricingId(entity.getPricingId());
        dto.setBatchLotNumber(entity.getBatchLotNumber());
        dto.setManufacturerName(entity.getManufacturerName());
        dto.setManufacturingDate(entity.getManufacturingDate());
        dto.setExpiryDate(entity.getExpiryDate());
        dto.setStorageCondition(entity.getStorageCondition());
        dto.setStockQuantity(entity.getStockQuantity());
        dto.setPricePerUnit(entity.getPricePerUnit());
        dto.setMrp(entity.getMrp());
        dto.setDiscountPercentage(entity.getDiscountPercentage());
        dto.setGstPercentage(entity.getGstPercentage());
        dto.setMinimumPurchaseQuantity(entity.getMinimumPurchaseQuantity());
        dto.setAdditionalDiscount(entity.getAdditionalDiscount());
        dto.setFinalPrice(entity.getFinalPrice());
        dto.setHsnCode(entity.getHsnCode());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setModifiedBy(entity.getModifiedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setModifiedDate(entity.getModifiedDate());

        return dto;
    }

    public static PricingDetailsDrug toPricingEntity(PricingDetailsDrugDto dto) {
        PricingDetailsDrug entity = new PricingDetailsDrug();
        entity.setPricingId(dto.getPricingId());
        entity.setBatchLotNumber(dto.getBatchLotNumber());
        entity.setManufacturerName(dto.getManufacturerName());
        entity.setManufacturingDate(dto.getManufacturingDate());
        entity.setExpiryDate(dto.getExpiryDate());
        entity.setStorageCondition(dto.getStorageCondition());
        entity.setStockQuantity(dto.getStockQuantity());
        entity.setPricePerUnit(dto.getPricePerUnit());
        entity.setMrp(dto.getMrp());
        entity.setDiscountPercentage(dto.getDiscountPercentage());
        entity.setGstPercentage(dto.getGstPercentage());
        entity.setMinimumPurchaseQuantity(dto.getMinimumPurchaseQuantity());
        entity.setFinalPrice(dto.getFinalPrice());
        entity.setHsnCode(dto.getHsnCode());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setModifiedBy(dto.getModifiedBy());
        entity.setCreatedDate(dto.getCreatedDate());
        entity.setModifiedDate(dto.getModifiedDate());

        return entity;
    }
}
