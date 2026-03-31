package com.example.pharmaaggregatorserver.controller.product;

import com.example.pharmaaggregatorserver.dto.product.DosageDto;
import com.example.pharmaaggregatorserver.dto.product.ProductDetailsDto;
import com.example.pharmaaggregatorserver.dto.product.TherapeuticSubcategoryDto;
import com.example.pharmaaggregatorserver.response.ApiResponse;
import com.example.pharmaaggregatorserver.security.UserDetailsImpl;
import com.example.pharmaaggregatorserver.service.product.ProductDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductDetailsController {

    private final ProductDetailsService productService;

    @PostMapping("/create")
    public ResponseEntity<ProductDetailsDto> createProduct(
            @RequestBody ProductDetailsDto dto,
            Authentication authentication
    ) {
        UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = user.getId();
        System.out.println("user Id is: "+userId);
        ProductDetailsDto response = productService.createProduct(dto, userId);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/getAll")
    public ResponseEntity<List<ProductDetailsDto>> getMyProducts(
            Authentication authentication
    ) {
        UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = user.getId();

        List<ProductDetailsDto> products = productService.getAllProducts(userId);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/getById/{productId}")
    public ResponseEntity<ProductDetailsDto> getProductById(
            @PathVariable String productId,
            Authentication authentication
    ) {
        UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = user.getId();

        ProductDetailsDto product = productService.getProductById(productId, userId);
        return ResponseEntity.ok(product);
    }


    @DeleteMapping("/delete/{productId}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable String productId,
            Authentication authentication
    ) {
        UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = user.getId();

        productService.deleteProductById(productId, userId);

        return ResponseEntity.ok(
                new ApiResponse<>("SUCCESS", "Product deleted successfully", null)
        );
    }

    @PutMapping("/update/{productId}")
    public ResponseEntity<ApiResponse<ProductDetailsDto>> updateProduct(
            @PathVariable String productId,
            @RequestBody ProductDetailsDto dto,
            Authentication authentication
    ) {

        UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = user.getId();

        ProductDetailsDto updatedProduct =
                productService.updateProduct(productId, dto, userId);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "SUCCESS",
                        "Product updated successfully",
                        updatedProduct
                )
        );
    }

    @GetMapping("/subcategories/{categoryId}")
    public ResponseEntity<List<TherapeuticSubcategoryDto>>
    getSubcategories(@PathVariable String categoryId) {
        return ResponseEntity.ok(
                productService.getSubcategories(categoryId)
        );
    }


}
