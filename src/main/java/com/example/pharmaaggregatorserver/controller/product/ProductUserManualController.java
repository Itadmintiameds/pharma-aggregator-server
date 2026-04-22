package com.example.pharmaaggregatorserver.controller.product;

import com.example.pharmaaggregatorserver.response.ApiResponse;
import com.example.pharmaaggregatorserver.service.product.ProductUserManualService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/userManual")
@RequiredArgsConstructor
public class ProductUserManualController {

    private final ProductUserManualService manualService;

    @PostMapping("/{productAttributeId}")
    public ResponseEntity<ApiResponse<String>> uploadManual(
            @PathVariable String productAttributeId,
            @RequestParam("file") MultipartFile file) {

        String url = manualService.uploadManual(productAttributeId, file);

        return ResponseEntity.ok(
                new ApiResponse<>("SUCCESS", "Manual uploaded successfully", url)
        );
    }


}
