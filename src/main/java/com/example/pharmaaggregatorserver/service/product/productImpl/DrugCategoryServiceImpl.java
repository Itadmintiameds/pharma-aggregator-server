package com.example.pharmaaggregatorserver.service.product.productImpl;

import com.example.pharmaaggregatorserver.dto.product.DrugCategoryDto;
import com.example.pharmaaggregatorserver.entity.product.DrugCategory;
import com.example.pharmaaggregatorserver.repository.product.DrugCategoryRepository;
import com.example.pharmaaggregatorserver.service.product.DrugCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DrugCategoryServiceImpl implements DrugCategoryService {

    private final DrugCategoryRepository drugCategoryRepository;

    @Override
    public List<DrugCategoryDto> getAllDrugCategories() {

        return drugCategoryRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private DrugCategoryDto toDto(DrugCategory entity) {

        DrugCategoryDto dto = new DrugCategoryDto();
        dto.setCategoryId(entity.getCategoryId());
        dto.setCategoryName(entity.getCategoryName());
        dto.setExample(entity.getExample());
        dto.setPrimaryUse(entity.getPrimaryUse());

        return dto;
    }
}
