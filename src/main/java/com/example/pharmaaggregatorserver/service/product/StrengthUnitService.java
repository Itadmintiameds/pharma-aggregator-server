package com.example.pharmaaggregatorserver.service.product;

import com.example.pharmaaggregatorserver.dto.product.StrengthUnitDto;

import java.util.List;

public interface StrengthUnitService {
    List<StrengthUnitDto> getByCategoryId(Long categoryId);

    StrengthUnitDto getById(Long strengthUnitId);
}
