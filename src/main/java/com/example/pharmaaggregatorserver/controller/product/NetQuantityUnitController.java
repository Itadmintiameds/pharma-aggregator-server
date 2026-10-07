package com.example.pharmaaggregatorserver.controller.product;

import com.example.pharmaaggregatorserver.entity.product.CosmeticPersonalCareMasters.ProductsFormMaster;
import com.example.pharmaaggregatorserver.entity.product.NetQuantityUnit;

import com.example.pharmaaggregatorserver.service.product.productImpl.NetQuantityUnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/net-quantity-units")
@RequiredArgsConstructor
public class NetQuantityUnitController {

    private final NetQuantityUnitService netQuantityUnitService;

    // Endpoint to get all units with category_id = 4
    @GetMapping("/{categoryId}")
    public ResponseEntity<List<NetQuantityUnit>> getUnitsByCategory(@PathVariable Long categoryId) {
        List<NetQuantityUnit> units = netQuantityUnitService.getUnitsByCategoryId(categoryId);
        return ResponseEntity.ok(units);
    }

    @GetMapping("/productsform")
    public ResponseEntity<List<ProductsFormMaster>> getAllForms() {
        List<ProductsFormMaster> forms = netQuantityUnitService.getAllForms();
        return ResponseEntity.ok(forms);
    }
}