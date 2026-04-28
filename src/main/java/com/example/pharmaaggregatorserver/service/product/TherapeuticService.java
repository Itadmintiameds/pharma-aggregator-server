package com.example.pharmaaggregatorserver.service.product;

import com.example.pharmaaggregatorserver.dto.product.TherapeuticCategoryMasterDto;
import com.example.pharmaaggregatorserver.dto.product.TherapeuticSubcategoryMasterDto;

import java.util.List;

public interface TherapeuticService {

    List<TherapeuticCategoryMasterDto> getAllCategories();

    List<TherapeuticSubcategoryMasterDto> getSubcategoriesByCategory(String categoryId);

    TherapeuticCategoryMasterDto getTherapeuticCategoryById(String therapeuticCategoryId);

    TherapeuticSubcategoryMasterDto getTherapeuticSubcategoryById(String therapeuticSubcategoryId);

}
