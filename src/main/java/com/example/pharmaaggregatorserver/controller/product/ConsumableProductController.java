package com.example.pharmaaggregatorserver.controller.product;

import com.example.pharmaaggregatorserver.dto.product.ConsumableProductRequestDTO;
import com.example.pharmaaggregatorserver.response.ApiResponse;
import com.example.pharmaaggregatorserver.service.product.ConsumableProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ConsumableProductController {

    private final ConsumableProductService productService;

    @PostMapping("/create-full-product")
    public ResponseEntity<ApiResponse> createFullProduct(@RequestBody ConsumableProductRequestDTO dto) {

        String productId = productService.createFullProduct(dto);

        ApiResponse response = new ApiResponse(
                "SUCCESS",
                "Product created successfully",
                productId
        );

        return ResponseEntity.ok(response);
    }
}