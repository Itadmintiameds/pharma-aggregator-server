package com.example.pharmaaggregatorserver.mapper.product;

import com.example.pharmaaggregatorserver.dto.product.PackagingDetailsDto;
import com.example.pharmaaggregatorserver.entity.product.PackType;
import com.example.pharmaaggregatorserver.entity.product.PackTypeUnitMaster;
import com.example.pharmaaggregatorserver.entity.product.PackagingDetails;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class PackagingDetailsMapper {

    private final PackTypeUnitMasterMapper packTypeUnitMasterMapper;

    public PackagingDetails toEntity(PackagingDetailsDto dto) {
        if (dto == null) return null;

        PackagingDetails entity = new PackagingDetails();
        entity.setPackagingId(dto.getPackagingId());
        entity.setUnitPerPack(dto.getUnitPerPack());
        entity.setNumberOfPacks(dto.getNumberOfPacks());
        entity.setPackSize(dto.getPackSize());
        entity.setMinimumOrderQuantity(dto.getMinimumOrderQuantity());
        entity.setMaximumOrderQuantity(dto.getMaximumOrderQuantity());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setModifiedBy(dto.getModifiedBy());
        entity.setCreatedDate(dto.getCreatedDate());
        entity.setModifiedDate(dto.getModifiedDate());

        if (dto.getPackId() != null) {
            PackType packType = new PackType();
            packType.setPackId(dto.getPackId());
            entity.setPackType(packType);
        }

        if (dto.getPackTypeUnitId() != null) {
            PackTypeUnitMaster unitMaster = new PackTypeUnitMaster();
            unitMaster.setPackTypeUnitId(dto.getPackTypeUnitId());
            entity.setPackTypeUnitMaster(unitMaster);
        }

        return entity;

    }


    public PackagingDetailsDto toDTO(PackagingDetails entity) {
        if (entity == null) return null;

        PackagingDetailsDto dto = new PackagingDetailsDto();
        dto.setPackagingId(entity.getPackagingId());
        dto.setUnitPerPack(entity.getUnitPerPack());
        dto.setNumberOfPacks(entity.getNumberOfPacks());
        dto.setPackSize(entity.getPackSize());
        dto.setMinimumOrderQuantity(entity.getMinimumOrderQuantity());
        dto.setMaximumOrderQuantity(entity.getMaximumOrderQuantity());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setModifiedBy(entity.getModifiedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setModifiedDate(entity.getModifiedDate());
        dto.setPackId(
                entity.getPackType() != null ? entity.getPackType().getPackId() : null
        );
        if (entity.getPackTypeUnitMaster() != null) {
            dto.setPackTypeUnitId(
                    entity.getPackTypeUnitMaster().getPackTypeUnitId()
            );

            dto.setPackTypeUnitMasterDto(
                    packTypeUnitMasterMapper.toDto(entity.getPackTypeUnitMaster()));
        }
        return dto;
    }


}
