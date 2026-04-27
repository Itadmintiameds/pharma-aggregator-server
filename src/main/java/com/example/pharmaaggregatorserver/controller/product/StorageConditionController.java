package com.example.pharmaaggregatorserver.controller.product;

import com.example.pharmaaggregatorserver.dto.product.StorageConditionDropdownDTO;
import com.example.pharmaaggregatorserver.dto.product.StorageConditionResponseDTO;
import com.example.pharmaaggregatorserver.service.product.productImpl.StorageConditionServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/storageConditions")
@RequiredArgsConstructor
public class StorageConditionController {

    private final StorageConditionServiceImpl storageConditionService;

    @GetMapping("/{categoryId}")
    public List<StorageConditionDropdownDTO> getByCategoryId(@PathVariable Long categoryId) {
        return storageConditionService.getByCategoryId(categoryId);
    }
}
