package com.example.pharmaaggregatorserver.controller.product;

import com.example.pharmaaggregatorserver.dto.product.TherapeuticCategoryMasterDto;
import com.example.pharmaaggregatorserver.dto.product.TherapeuticSubcategoryMasterDto;
import com.example.pharmaaggregatorserver.service.product.TherapeuticService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/therapeutic")
@RequiredArgsConstructor
public class TherapeuticController {

    private final TherapeuticService therapeuticService;

    @GetMapping("/therapeuticCategories")
    public List<TherapeuticCategoryMasterDto> getAllCategories() {
        return therapeuticService.getAllCategories();
    }

    @GetMapping("/therapeuticSubcategories/{categoryId}")
    public List<TherapeuticSubcategoryMasterDto> getSubcategories(
            @PathVariable String categoryId) {

        return therapeuticService.getSubcategoriesByCategory(categoryId);
    }
}


