package com.example.pharmaaggregatorserver.service.product.productImpl;

import com.example.pharmaaggregatorserver.entity.product.Category;
import com.example.pharmaaggregatorserver.entity.product.ProductAttributeFoodInfant;
import com.example.pharmaaggregatorserver.entity.product.ProductAttributeSupplementsOrNutraceuticals;
import com.example.pharmaaggregatorserver.exception.NotFoundException;
import com.example.pharmaaggregatorserver.repository.product.CategoryRepository;
import com.example.pharmaaggregatorserver.repository.product.ProductAttributeFoodInfantRepository;
import com.example.pharmaaggregatorserver.repository.product.ProductAttributeSupplementsOrNutraceuticalsRepository;
import com.example.pharmaaggregatorserver.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class NutritionalInformationImageService {

    public static final String FOOD_INFANT_NUTRITION = "Food & Infant Nutrition";
    public static final String SUPPLEMENTS_NUTRACEUTICALS = "Supplements / Nutraceuticals";
    private static final DateTimeFormatter TS_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final ProductAttributeFoodInfantRepository foodInfantRepository;
    private final S3Service s3Service;
    private final CategoryRepository categoryRepository;
    private final ProductAttributeSupplementsOrNutraceuticalsRepository supplementsOrNutraceuticalsRepository;

    public String uploadNutritionalInfoImage(String productAttributeId, Long categoryId, MultipartFile file) {
        validateFile(file);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + categoryId));

        if (category.getCategoryName() == null) {
            throw new IllegalStateException("Category has no name configured");
        }

        String categoryName = category.getCategoryName();

        String now = LocalDateTime.now().format(TS_FORMATTER);
        if (categoryName.equalsIgnoreCase(FOOD_INFANT_NUTRITION)) {
            ProductAttributeFoodInfant productAttribute = foodInfantRepository.findById(productAttributeId)
                    .orElseThrow(() -> new NotFoundException("Product Attribute not found: " + productAttributeId));

            deleteIfRealUrl(productAttribute.getNutritionalInformationImageUrl());

            String key = buildS3Key(
                    productAttribute.getProductDetails().getProductId(),
                    "food-infant-nutrition",
                    productAttributeId,
                    now,
                    file
            );
            String imageUrl = s3Service.uploadFile(key, file);

            productAttribute.setNutritionalInformationImageUrl(imageUrl);
            foodInfantRepository.save(productAttribute);
            return imageUrl;
        }

        if (categoryName.equalsIgnoreCase(SUPPLEMENTS_NUTRACEUTICALS)) {
            ProductAttributeSupplementsOrNutraceuticals productAttribute = supplementsOrNutraceuticalsRepository.findById(productAttributeId)
                    .orElseThrow(() -> new NotFoundException("Product Attribute not found: " + productAttributeId));

            deleteIfRealUrl(productAttribute.getNutritionalInformationImageUrl());

            String key = buildS3Key(
                    productAttribute.getProductDetails().getProductId(),
                    "supplements-or-nutraceuticals",
                    productAttributeId,
                    now,
                    file
            );

            String imageUrl = s3Service.uploadFile(key, file);

            productAttribute.setNutritionalInformationImageUrl(imageUrl);
            supplementsOrNutraceuticalsRepository.save(productAttribute);
            return imageUrl;
        }

        throw new IllegalArgumentException("Unsupported category: " + categoryName);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }
    }

    private String buildS3Key(String productId, String folder, String productAttributeId,
                              String timestamp, MultipartFile file) {
        return String.format("products/%s/%s/%s/nutritional_information/%s.%s",
                productId, folder, productAttributeId, "NUTRITIONAL_INFORMATION_" + timestamp, extension(file));
    }

    /**
     * Extracts file extension from the original filename.
     * Falls back to "bin" when the extension cannot be determined.
     */
    private String extension(MultipartFile file) {
        String original = Objects.requireNonNullElse(file.getOriginalFilename(), "");
        int dot = original.lastIndexOf('.');
        return (dot >= 0 && dot < original.length() - 1)
                ? original.substring(dot + 1).toLowerCase()
                : "bin";
    }

    /**
     * Deletes the existing S3 object only when {@code url} is a real S3 URL.
     * Skips deletion when the value is null, blank, or a local placeholder
     * like "/certs/cdsco.pdf" or "/docs/accucheck-pro.pdf" sent during createProduct.
     */
    private void deleteIfRealUrl(String url) {
        if (url == null || url.isBlank()) return;
        if (!url.startsWith("https://")) return;        // skip local placeholders
        try {
            s3Service.deleteFile(s3Service.extractKeyFromUrl(url));
        } catch (Exception e) {
            log.warn("Could not delete old S3 file (url={}): {}", url, e.getMessage());
        }
    }
}
