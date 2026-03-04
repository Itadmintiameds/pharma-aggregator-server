package com.example.pharmaaggregatorserver.controller.product;

import com.example.pharmaaggregatorserver.dto.product.DrugCategoryDto;
import com.example.pharmaaggregatorserver.service.product.DrugCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/drugCategory")
@RequiredArgsConstructor
public class DrugCategoryController {

    private final DrugCategoryService drugCategoryService;

    @GetMapping("/getAll")
    public ResponseEntity<List<DrugCategoryDto>> getAllCategories() {
        return ResponseEntity.ok(drugCategoryService.getAllDrugCategories());
    }
}
