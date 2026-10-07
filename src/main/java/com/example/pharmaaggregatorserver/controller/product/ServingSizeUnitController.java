package com.example.pharmaaggregatorserver.controller.product;

import com.example.pharmaaggregatorserver.service.product.ServingSizeUnitService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/serving-size")
@AllArgsConstructor
public class ServingSizeUnitController {

    private final ServingSizeUnitService servingSizeUnitService;

    @GetMapping
    public ResponseEntity<?> findAll() {
        return ResponseEntity.ok(servingSizeUnitService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        return ResponseEntity.ok(servingSizeUnitService.findById(id));
    }

    @GetMapping("/dosageForm/{dosageFormId}")
    public ResponseEntity<?> findByDosageFormId(@PathVariable("dosageFormId") Long dosageFormId) {
        return ResponseEntity.ok(servingSizeUnitService.findByDosageFormId(dosageFormId));
    }

    @GetMapping("/productForm/{productFormId}")
    public ResponseEntity<?> findByProductFormId(@PathVariable("productFormId") Long productFormId) {
        return ResponseEntity.ok(servingSizeUnitService.findByProductFormId(productFormId));
    }

}
