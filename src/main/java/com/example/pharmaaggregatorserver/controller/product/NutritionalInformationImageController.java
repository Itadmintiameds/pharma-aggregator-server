package com.example.pharmaaggregatorserver.controller.product;

import com.example.pharmaaggregatorserver.service.product.productImpl.NutritionalInformationImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/nutritionalInformationImage")
@RequiredArgsConstructor
public class NutritionalInformationImageController {

    private final NutritionalInformationImageService nutritionalInformationImageService;

    @PostMapping(
            value = "/{productAttributeId}",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<?> uploadNutritionalImage(
            @PathVariable String productAttributeId,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam("images") MultipartFile file
    ) {

        String imageUrl =
                nutritionalInformationImageService.uploadNutritionalInfoImage(
                        productAttributeId,
                        categoryId,
                        file
                );

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Image uploaded successfully");
        response.put("imageUrl", imageUrl);

        return ResponseEntity.ok(response);
    }
}