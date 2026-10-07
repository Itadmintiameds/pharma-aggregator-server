package com.example.pharmaaggregatorserver.mapper.product;

import com.example.pharmaaggregatorserver.dto.product.ProductImageDto;
import com.example.pharmaaggregatorserver.entity.product.ProductImage;
import org.springframework.stereotype.Component;

@Component
public class ProductImageMapper {

    public ProductImage toEntity(ProductImageDto dto) {
        if (dto == null) return null;

        ProductImage entity = new ProductImage();
        entity.setProductImageId(dto.getProductImageId());
        entity.setProductImage(dto.getProductImage());

        return entity;
    }

    public ProductImageDto toDto(ProductImage entity) {
        if (entity == null) return null;

        ProductImageDto dto = new ProductImageDto();
        dto.setProductImageId(entity.getProductImageId());
        dto.setProductImage(entity.getProductImage());

        return dto;
    }
}
