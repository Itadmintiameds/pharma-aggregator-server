package com.example.pharmaaggregatorserver.service.product.productImpl;

import com.example.pharmaaggregatorserver.entity.product.ProductAttributeFoodInfant;
import com.example.pharmaaggregatorserver.repository.product.ProductAttributeFoodInfantRepository;
import com.example.pharmaaggregatorserver.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class NutritionalInformationImageSevice {

    private final ProductAttributeFoodInfantRepository foodInfantRepository;
    private final S3Service s3Service;

    public String uploadNutritionalInfoImage(
            String productAttributeId,
            MultipartFile file
    ) {

        ProductAttributeFoodInfant productAttribute =
                foodInfantRepository.findById(productAttributeId)
                        .orElseThrow(() ->
                                new RuntimeException("Product Attribute not found"));

        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        if (file.getContentType() == null ||
                !file.getContentType().startsWith("image/")) {

            throw new RuntimeException("Only image files are allowed");
        }

        String key = "nutritional-information-image/"
                + productAttributeId + "/"
                + System.currentTimeMillis()
                + "_" + file.getOriginalFilename();

        String imageUrl = s3Service.uploadFile(key, file);

        productAttribute.setNutritionalInformationImageUrl(imageUrl);

        foodInfantRepository.save(productAttribute);

        return imageUrl;
    }
}
