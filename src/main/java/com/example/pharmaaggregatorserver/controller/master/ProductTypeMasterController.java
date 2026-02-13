package com.example.pharmaaggregatorserver.controller.master;

import com.example.pharmaaggregatorserver.dto.master.ResponseDTO.ProductTypeResponseDTO;
import com.example.pharmaaggregatorserver.service.master.ProductTypeMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/master/product-types")
@RequiredArgsConstructor
public class ProductTypeMasterController {

    private final ProductTypeMasterService productTypeMasterService;

    @GetMapping
    public ResponseEntity<List<ProductTypeResponseDTO>> getAllProductTypes() {
        return ResponseEntity.ok(productTypeMasterService.getAllProductTypes());
    }
}