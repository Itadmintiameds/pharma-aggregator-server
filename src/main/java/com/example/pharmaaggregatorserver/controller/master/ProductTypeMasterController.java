package com.example.pharmaaggregatorserver.controller.master;

import com.example.pharmaaggregatorserver.entity.master.ProductTypeMaster;
import com.example.pharmaaggregatorserver.service.serviceImpl.master.ProductTypeMasterServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/product-types")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProductTypeMasterController {

    private final ProductTypeMasterServiceImpl productTypeMasterService;

    /**
     * GET /api/v1/product-types
     * Get all product types
     */
    @GetMapping
    public ResponseEntity<List<ProductTypeMaster>> getAllProductTypes() {
        List<ProductTypeMaster> productTypes = productTypeMasterService.getAllProductTypes();
        return ResponseEntity.ok(productTypes);
    }
}