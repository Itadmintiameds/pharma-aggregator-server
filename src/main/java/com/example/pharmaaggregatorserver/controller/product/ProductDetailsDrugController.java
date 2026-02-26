package com.example.pharmaaggregatorserver.controller.product;

import com.example.pharmaaggregatorserver.dto.product.CreateProductRequestDto;
import com.example.pharmaaggregatorserver.dto.product.ProductDetailsDrugDto;
import com.example.pharmaaggregatorserver.entity.product.ProductDetailsDrug;
import com.example.pharmaaggregatorserver.mapper.product.ProductDetailsDrugMapper;
import com.example.pharmaaggregatorserver.service.product.ProductDetailsDrugService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductDetailsDrugController {

    private final ProductDetailsDrugService productDetailsDrugService;

    @PostMapping("/create")
    public ResponseEntity<ProductDetailsDrugDto> createProduct(
            @RequestBody CreateProductRequestDto request) {

        ProductDetailsDrugDto created =
                productDetailsDrugService.createProduct(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }



    @GetMapping("/getAll")
    public ResponseEntity<List<ProductDetailsDrugDto>> getAllProducts() {
        return ResponseEntity.ok(productDetailsDrugService.getAllProducts());
    }

    @GetMapping("/getById/{productId}")
    public ResponseEntity<ProductDetailsDrugDto> getProductById(
            @PathVariable String productId) {

        return ResponseEntity.ok(
                productDetailsDrugService.getProductById(productId)
        );
    }


    @DeleteMapping("/delete/{productId}")
    public ResponseEntity<Map<String, String>> deleteProduct(
            @PathVariable String productId) {

        productDetailsDrugService.deleteProduct(productId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Product deleted successfully");
        response.put("productId", productId.toString());

        return ResponseEntity.ok(response);
    }


    @PutMapping("/update/{productId}")
    public ResponseEntity<ProductDetailsDrugDto> updateProduct(
            @PathVariable String productId,
            @RequestBody ProductDetailsDrugDto dto
    ) {
        ProductDetailsDrug updated =
                productDetailsDrugService.updateProduct(productId, dto);

        return ResponseEntity.ok(ProductDetailsDrugMapper.toDto(updated));
    }


}
