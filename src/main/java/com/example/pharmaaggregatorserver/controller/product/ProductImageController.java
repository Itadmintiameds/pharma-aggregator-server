package com.example.pharmaaggregatorserver.controller.product;

import com.example.pharmaaggregatorserver.service.product.productImpl.ProductImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/product-images")
public class ProductImageController {

        @Autowired
        private ProductImageService productImageService;

        @PostMapping(value = "/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<?> uploadImages(
                @PathVariable String productId,
                @RequestPart("images") List<MultipartFile> files
        ) {
            return ResponseEntity.ok(productImageService.uploadImages(productId, files));
        }


    @GetMapping("/getImg/{productId}")
    public ResponseEntity<?> getImages(@PathVariable String productId) {

        List<String> imageUrls =
                productImageService.getImagesByProductId(productId);

        return ResponseEntity.ok(imageUrls);
    }
    }

