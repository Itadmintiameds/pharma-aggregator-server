package com.example.pharmaaggregatorserver.mapper.product;

import com.example.pharmaaggregatorserver.dto.product.ProductUserManualDto;
import com.example.pharmaaggregatorserver.entity.product.ProductUserManual;

public class ProductUserManualMapper {

    public ProductUserManual toEntity(ProductUserManualDto dto) {
        if (dto == null) return null;

        ProductUserManual entity = new ProductUserManual();
        entity.setUserManualId(dto.getUserManualId());
        entity.setUserManualUrl(dto.getUserManualUrl());

        return entity;
    }

    public ProductUserManualDto toDto(ProductUserManual entity) {
        if (entity == null) return null;

        ProductUserManualDto dto = new ProductUserManualDto();
        dto.setUserManualId(entity.getUserManualId());
        dto.setUserManualUrl(entity.getUserManualUrl());

        return dto;
    }
}
