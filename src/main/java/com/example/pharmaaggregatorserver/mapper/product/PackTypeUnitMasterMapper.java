package com.example.pharmaaggregatorserver.mapper.product;

import com.example.pharmaaggregatorserver.dto.product.PackTypeUnitMasterDto;
import com.example.pharmaaggregatorserver.entity.product.PackTypeUnitMaster;
import org.springframework.stereotype.Component;

@Component
public class PackTypeUnitMasterMapper {

    public PackTypeUnitMasterDto toDto(PackTypeUnitMaster entity) {
        return new PackTypeUnitMasterDto(
                entity.getPackTypeUnitId(),
                entity.getPackTypeUnitName(),
                entity.getIsActive()
        );
    }

}

