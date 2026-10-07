package com.example.pharmaaggregatorserver.mapper.product;

import com.example.pharmaaggregatorserver.dto.product.AdditionalDiscountDto;
import com.example.pharmaaggregatorserver.dto.product.PricingDetailsDto;
import com.example.pharmaaggregatorserver.dto.product.SpecialSchemesDto;
import com.example.pharmaaggregatorserver.entity.product.SpecialSchemes;
import com.example.pharmaaggregatorserver.entity.product.AdditionalDiscount;

import com.example.pharmaaggregatorserver.entity.product.PackagingDetails;
import com.example.pharmaaggregatorserver.entity.product.PricingDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PricingDetailsMapper {

    private final AdditionalDiscountMapper additionalDiscountMapper;
    private final SpecialSchemesMapper specialSchemesMapper;

    public PricingDetails toEntity(PricingDetailsDto dto) {
        if (dto == null) return null;

        PricingDetails entity = new PricingDetails();
        entity.setPricingId(dto.getPricingId());
        entity.setBatchLotNumber(dto.getBatchLotNumber());
        entity.setManufacturingDate(dto.getManufacturingDate());
        entity.setExpiryDate(dto.getExpiryDate());
        entity.setStockQuantity(dto.getStockQuantity());
        entity.setDateOfStockEntry(dto.getDateOfStockEntry());
        entity.setSellingPrice(dto.getSellingPrice());
        entity.setMrp(dto.getMrp());
        entity.setDiscountPercentage(dto.getDiscountPercentage());
        entity.setGstPercentage(dto.getGstPercentage());
        entity.setFinalPrice(dto.getFinalPrice());
        entity.setHsnCode(dto.getHsnCode());
        entity.setShelfLifeMonths(dto.getShelfLifeMonths());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setModifiedBy(dto.getModifiedBy());
        entity.setCreatedDate(dto.getCreatedDate());
        entity.setModifiedDate(dto.getModifiedDate());

        if (dto.getPackagingId() != null && !dto.getPackagingId().isBlank()) {
            PackagingDetails packaging = new PackagingDetails();
            packaging.setPackagingId(dto.getPackagingId());
            entity.setPackagingDetails(packaging);
        }

        if (dto.getAdditionalDiscounts() != null) {
            Set<AdditionalDiscount> discounts = dto.getAdditionalDiscounts()
                    .stream()
                    .map(additionalDiscountMapper::toEntity)
                    .collect(Collectors.toSet());

            discounts.forEach(d -> d.setPricingDetails(entity));

            entity.setAdditionalDiscounts(discounts);
        }

        if (dto.getSpecialSchemes() != null) {
            Set<SpecialSchemes> specialSchemes = dto.getSpecialSchemes()
                    .stream()
                    .map(specialSchemesMapper::toEntity)
                    .collect(Collectors.toSet());

            specialSchemes.forEach(d -> d.setPricingDetails(entity));

            entity.setSpecialSchemes(specialSchemes);
        }

        return entity;
    }

    public PricingDetailsDto toDTO(PricingDetails entity) {
        if (entity == null) return null;

        PricingDetailsDto dto = new PricingDetailsDto();
        dto.setPricingId(entity.getPricingId());
        dto.setPackagingId(entity.getPackagingDetails() != null ? entity.getPackagingDetails().getPackagingId() : null);
        dto.setBatchLotNumber(entity.getBatchLotNumber());
        dto.setManufacturingDate(entity.getManufacturingDate());
        dto.setExpiryDate(entity.getExpiryDate());
//        dto.setStorageCondition(entity.getStorageCondition());
        dto.setStockQuantity(entity.getStockQuantity());
        dto.setDateOfStockEntry(entity.getDateOfStockEntry());
        dto.setSellingPrice(entity.getSellingPrice());
        dto.setMrp(entity.getMrp());
        dto.setDiscountPercentage(entity.getDiscountPercentage());
        dto.setGstPercentage(entity.getGstPercentage());
        dto.setFinalPrice(entity.getFinalPrice());
        dto.setHsnCode(entity.getHsnCode());
        dto.setShelfLifeMonths(entity.getShelfLifeMonths());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setModifiedBy(entity.getModifiedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setModifiedDate(entity.getModifiedDate());
        dto.setDeletedBy(entity.getDeletedBy());
        dto.setDeletedAt(entity.getDeletedAt());

        if (entity.getAdditionalDiscounts() != null) {
            Set<AdditionalDiscountDto> discounts = entity.getAdditionalDiscounts()
                    .stream()
                    .map(additionalDiscountMapper::toDTO)
                    .collect(Collectors.toSet());

            dto.setAdditionalDiscounts(discounts);
        }

        if (entity.getSpecialSchemes() != null) {
            Set<SpecialSchemesDto> specialSchemesDtos = entity.getSpecialSchemes()
                    .stream()
                    .map(specialSchemesMapper::toDTO)
                    .collect(Collectors.toSet());

            dto.setSpecialSchemes(specialSchemesDtos);
        }

        return dto;
    }
}
