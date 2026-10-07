package com.example.pharmaaggregatorserver.service.product.productImpl;

import com.example.pharmaaggregatorserver.dto.product.TherapeuticCategoryMasterDto;
import com.example.pharmaaggregatorserver.dto.product.TherapeuticSubcategoryMasterDto;
import com.example.pharmaaggregatorserver.entity.product.TherapeuticCategoryMaster;
import com.example.pharmaaggregatorserver.entity.product.TherapeuticSubcategoryMaster;
import com.example.pharmaaggregatorserver.repository.product.TherapeuticCategoryRepository;
import com.example.pharmaaggregatorserver.repository.product.TherapeuticSubcategoryRepository;
import com.example.pharmaaggregatorserver.service.product.TherapeuticService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TherapeuticServiceImpl implements TherapeuticService {

    private final TherapeuticCategoryRepository categoryRepo;
    private final TherapeuticSubcategoryRepository subcategoryRepo;

    @Override
    public List<TherapeuticCategoryMasterDto> getAllCategories() {
        List<TherapeuticCategoryMaster> categories = categoryRepo.findAll();

        return categories.stream()
                .map(c -> new TherapeuticCategoryMasterDto(
                        c.getTherapeuticCategoryId(),
                        c.getTherapeuticCategory(),
                        c.getCategory().getCategoryId(),
                        c.getCategory().getCategoryName()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<TherapeuticCategoryMasterDto> getTherapeuticCategoriesByCategoryId(Long categoryId) {
        List<TherapeuticCategoryMaster> categories = categoryRepo.findByCategory_CategoryId(categoryId);

        return categories.stream()
                .map(c -> new TherapeuticCategoryMasterDto(
                        c.getTherapeuticCategoryId(),
                        c.getTherapeuticCategory(),
                        c.getCategory().getCategoryId(),
                        c.getCategory().getCategoryName()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<TherapeuticSubcategoryMasterDto> getSubcategoriesByCategory(String categoryId) {

        List<TherapeuticSubcategoryMaster> subcategories =
                subcategoryRepo.findByTherapeuticCategoryMaster_TherapeuticCategoryId(categoryId);

        return subcategories.stream()
                .map(s -> new TherapeuticSubcategoryMasterDto(
                        s.getTherapeuticSubcategoryId(),
                        s.getTherapeuticSubcategory()
                ))
                .collect(Collectors.toList());
    }


    @Override
    public TherapeuticCategoryMasterDto getTherapeuticCategoryById(String therapeuticCategoryId) {

        TherapeuticCategoryMaster category = categoryRepo.findById(therapeuticCategoryId)
                .orElseThrow(() -> new RuntimeException("Therapeutic Category not found"));

        return new TherapeuticCategoryMasterDto(
                category.getTherapeuticCategoryId(),
                category.getTherapeuticCategory(),
                category.getCategory().getCategoryId(),
                category.getCategory().getCategoryName()
        );
    }


    @Override
    public TherapeuticSubcategoryMasterDto getTherapeuticSubcategoryById(String therapeuticSubcategoryId) {

        TherapeuticSubcategoryMaster subcategory = subcategoryRepo.findById(therapeuticSubcategoryId)
                .orElseThrow(() -> new RuntimeException("Therapeutic Subcategory not found"));

        return new TherapeuticSubcategoryMasterDto(
                subcategory.getTherapeuticSubcategoryId(),
                subcategory.getTherapeuticSubcategory()
        );
    }
}
