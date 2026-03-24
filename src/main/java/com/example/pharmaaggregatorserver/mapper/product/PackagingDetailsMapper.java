package com.example.pharmaaggregatorserver.mapper.product;

import com.example.pharmaaggregatorserver.dto.product.PackagingDetailsDto;
import com.example.pharmaaggregatorserver.entity.product.PackagingDetails;
import org.springframework.stereotype.Component;

@Component
public class PackagingDetailsMapper {

    public PackagingDetails toEntity(PackagingDetailsDto dto){
        if (dto == null) return null;

        PackagingDetails entity = new PackagingDetails();
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


    public PackagingDetailsDto toDTO(PackagingDetails entity) {
        if (entity == null) return null;

        PackagingDetailsDto dto = new PackagingDetailsDto();
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


}
