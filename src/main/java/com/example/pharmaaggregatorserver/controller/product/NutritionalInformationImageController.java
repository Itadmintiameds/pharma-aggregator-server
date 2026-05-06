package com.example.pharmaaggregatorserver.controller.product;

import com.example.pharmaaggregatorserver.service.product.productImpl.NutritionalInformationImageSevice;
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

    private final NutritionalInformationImageSevice nutritionalInformationImageSevice;

    @PostMapping(
            value = "/{productAttributeId}",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<?> uploadNutritionalImage(
            @PathVariable String productAttributeId,
            @RequestParam("images") MultipartFile file
    ) {

        String imageUrl =
                nutritionalInformationImageSevice.uploadNutritionalInfoImage(
                        productAttributeId,
                        file
                );

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Image uploaded successfully");
        response.put("imageUrl", imageUrl);

        return ResponseEntity.ok(response);
    }
}