package com.example.pharmaaggregatorserver.service.product.productImpl;

import com.example.pharmaaggregatorserver.entity.product.ProductDetails;
import com.example.pharmaaggregatorserver.entity.product.ProductImage;
import com.example.pharmaaggregatorserver.repository.product.ProductDetailsRepository;
import com.example.pharmaaggregatorserver.repository.product.ProductImageRepository;
import com.example.pharmaaggregatorserver.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class ProductImageService {

    private final ProductDetailsRepository productRepo;
    private final ProductImageRepository productImageRepository;
    private final S3Service s3Service;

    public List<String> uploadImages(String productId, List<MultipartFile> files) {

        ProductDetails product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (product.getProductImages() == null) {
            product.setProductImages(new HashSet<>());
        }

        List<String> uploadedUrls = new ArrayList<>();

        for (MultipartFile file : files) {

            if (file.isEmpty()) {
                throw new RuntimeException("File is empty");
            }

            if (!file.getContentType().startsWith("image/")) {
                throw new RuntimeException("Only image files are allowed");
            }

            String key = "products/" + productId + "/"
                    + System.currentTimeMillis() + "_" + file.getOriginalFilename();

            String imageUrl = s3Service.uploadFile(key, file);

            ProductImage image = new ProductImage();
            image.setProductImage(imageUrl);
            image.setProductDetails(product);

            product.getProductImages().add(image);

            uploadedUrls.add(imageUrl);
        }

        productRepo.save(product);

        return uploadedUrls;
    }


    public List<String> getImagesByProductId(String productId) {

        if (!productRepo.existsById(productId)) {
            throw new RuntimeException("Product not found");
        }

        List<ProductImage> images =
                productImageRepository.findByProductDetails_ProductId(productId);

        return images.stream()
                .map(ProductImage::getProductImage)
                .collect(Collectors.toList());
    }

}
