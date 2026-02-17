package com.example.pharmaaggregatorserver.service.product;

import com.example.pharmaaggregatorserver.dto.product.DrugCategoryDto;

import java.util.List;

public interface DrugCategoryService {

    List<DrugCategoryDto> getAllDrugCategories();
}
