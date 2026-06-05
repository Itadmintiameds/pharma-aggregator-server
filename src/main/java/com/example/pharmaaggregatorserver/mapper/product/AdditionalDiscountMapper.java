package com.example.pharmaaggregatorserver.mapper.product;

import com.example.pharmaaggregatorserver.dto.product.AdditionalDiscountDto;
import com.example.pharmaaggregatorserver.entity.product.AdditionalDiscount;
import org.springframework.stereotype.Component;

@Component
public class AdditionalDiscountMapper {

    public AdditionalDiscount toEntity(AdditionalDiscountDto dto) {
        if (dto == null) return null;

        AdditionalDiscount entity = new AdditionalDiscount();
        entity.setAdditionalDiscountId(dto.getAdditionalDiscountId());
        entity.setMinimumPurchaseQuantity(dto.getMinimumPurchaseQuantity());
        entity.setAdditionalDiscountPercentage(dto.getAdditionalDiscountPercentage());
        entity.setEffectiveStartDate(dto.getEffectiveStartDate());
        entity.setEffectiveStartTime(dto.getEffectiveStartTime());
        entity.setEffectiveEndDate(dto.getEffectiveEndDate());
        entity.setEffectiveEndTime(dto.getEffectiveEndTime());
        entity.setDisplayOffer(dto.getDisplayOffer());
        return entity;
    }

    public AdditionalDiscountDto toDTO(AdditionalDiscount entity) {
        if (entity == null) return null;

        AdditionalDiscountDto dto = new AdditionalDiscountDto();
        dto.setAdditionalDiscountId(entity.getAdditionalDiscountId());
        dto.setMinimumPurchaseQuantity(entity.getMinimumPurchaseQuantity());
        dto.setAdditionalDiscountPercentage(entity.getAdditionalDiscountPercentage());
        dto.setEffectiveStartDate(entity.getEffectiveStartDate());
        dto.setEffectiveStartTime(entity.getEffectiveStartTime());
        dto.setEffectiveEndDate(entity.getEffectiveEndDate());
        dto.setEffectiveEndTime(entity.getEffectiveEndTime());
        dto.setDisplayOffer(entity.getDisplayOffer());
        return dto;
    }
}
