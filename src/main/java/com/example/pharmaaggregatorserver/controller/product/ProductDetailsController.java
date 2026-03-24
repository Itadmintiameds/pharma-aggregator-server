package com.example.pharmaaggregatorserver.controller.product;

import com.example.pharmaaggregatorserver.dto.product.ProductDetailsDto;
import com.example.pharmaaggregatorserver.security.UserDetailsImpl;
import com.example.pharmaaggregatorserver.service.product.ProductDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
