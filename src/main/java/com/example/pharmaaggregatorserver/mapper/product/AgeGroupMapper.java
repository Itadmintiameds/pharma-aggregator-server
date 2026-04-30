package com.example.pharmaaggregatorserver.mapper.product;

import com.example.pharmaaggregatorserver.dto.product.AgeGroupMasterDto;
import com.example.pharmaaggregatorserver.entity.product.AgeGroupMaster;
import org.springframework.stereotype.Component;

@Component
public class AgeGroupMapper {

    public AgeGroupMasterDto toDto(AgeGroupMaster entity) {
        return new AgeGroupMasterDto(
                entity.getAgeGroupId(),
                entity.getAgeGroup()
        );
    }

    public AgeGroupMaster toEntity(AgeGroupMasterDto dto) {
        return new AgeGroupMaster(
                dto.getAgeGroupId(),
                dto.getAgeGroup()
        );
    }
}

