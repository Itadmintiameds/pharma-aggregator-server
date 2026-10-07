package com.example.pharmaaggregatorserver.mapper.product;

import com.example.pharmaaggregatorserver.dto.product.StrengthUnitDto;
import com.example.pharmaaggregatorserver.entity.product.StrengthUnit;
import org.springframework.stereotype.Component;

@Component
public class StrengthUnitMapper {
    public StrengthUnitDto toDto(StrengthUnit entity) {
        return new StrengthUnitDto(
                entity.getStrengthUnitId(),
                entity.getUnitName(),
                entity.getIsActive()
        );
    }
}
