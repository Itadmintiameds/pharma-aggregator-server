package com.example.pharmaaggregatorserver.mapper.product;

import com.example.pharmaaggregatorserver.dto.product.AdditionalDiscountDto;
import com.example.pharmaaggregatorserver.dto.product.SpecialSchemesDto;
import com.example.pharmaaggregatorserver.entity.SpecialSchemes;
import com.example.pharmaaggregatorserver.entity.product.AdditionalDiscount;
import org.springframework.stereotype.Component;

@Component
public class SpecialSchemesMapper {

    public SpecialSchemes toEntity(SpecialSchemesDto dto) {
        if (dto == null) return null;

        SpecialSchemes entity = new SpecialSchemes();
        entity.setSpecialSchemesId(dto.getSpecialSchemesId());
        entity.setSchemeName(dto.getSchemeName());
        entity.setSchemeType(dto.getSchemeType());
        entity.setBuyQuantity(dto.getBuyQuantity());
        entity.setFreeQuantity(dto.getFreeQuantity());
        entity.setEffectiveStartDate(dto.getEffectiveStartDate());
        entity.setEffectiveStartTime(dto.getEffectiveStartTime());
        entity.setEffectiveEndDate(dto.getEffectiveEndDate());
        entity.setEffectiveEndTime(dto.getEffectiveEndTime());
        return entity;
    }


    public SpecialSchemesDto toDTO(SpecialSchemes entity) {
        if (entity == null) return null;

        SpecialSchemesDto dto = new SpecialSchemesDto();
        dto.setSpecialSchemesId(entity.getSpecialSchemesId());
        dto.setSchemeName(entity.getSchemeName());
        dto.setSchemeType(entity.getSchemeType());
        dto.setBuyQuantity(entity.getBuyQuantity());
        dto.setFreeQuantity(entity.getFreeQuantity());
        dto.setEffectiveStartDate(entity.getEffectiveStartDate());
        dto.setEffectiveStartTime(entity.getEffectiveStartTime());
        dto.setEffectiveEndDate(entity.getEffectiveEndDate());
        dto.setEffectiveEndTime(entity.getEffectiveEndTime());
        return dto;
    }
}
