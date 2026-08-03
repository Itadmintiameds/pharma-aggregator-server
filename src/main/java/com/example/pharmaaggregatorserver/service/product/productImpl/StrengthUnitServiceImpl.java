package com.example.pharmaaggregatorserver.service.product.productImpl;

import com.example.pharmaaggregatorserver.dto.product.StrengthUnitDto;
import com.example.pharmaaggregatorserver.entity.product.StrengthUnit;
import com.example.pharmaaggregatorserver.mapper.product.StrengthUnitMapper;
import com.example.pharmaaggregatorserver.repository.product.StrengthUnitRepository;
import com.example.pharmaaggregatorserver.service.product.StrengthUnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StrengthUnitServiceImpl implements StrengthUnitService {

    private final StrengthUnitRepository strengthUnitRepository;
    private final StrengthUnitMapper strengthUnitMapper;

    @Override
    @Transactional(readOnly = true)
    public List<StrengthUnitDto> getByCategoryId(Long categoryId) {
        return strengthUnitRepository.findByCategory_CategoryId(categoryId)
                .stream()
                .map(strengthUnitMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public StrengthUnitDto getById(Long strengthUnitId) {
        StrengthUnit strengthUnit = strengthUnitRepository.findById(strengthUnitId)
                .orElseThrow(() -> new RuntimeException("Strength Unit not found"));
        return strengthUnitMapper.toDto(strengthUnit);
    }
}
