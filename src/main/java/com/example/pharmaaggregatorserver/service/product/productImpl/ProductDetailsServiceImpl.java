package com.example.pharmaaggregatorserver.service.product.productImpl;

import com.example.pharmaaggregatorserver.dto.product.ProductDetailsDto;
import com.example.pharmaaggregatorserver.entity.product.Category;
import com.example.pharmaaggregatorserver.entity.product.Molecule;
import com.example.pharmaaggregatorserver.entity.product.ProductDetails;
import com.example.pharmaaggregatorserver.entity.seller.Seller;
import com.example.pharmaaggregatorserver.mapper.product.ProductDetailsMapper;
import com.example.pharmaaggregatorserver.repository.product.*;
import com.example.pharmaaggregatorserver.repository.seller.SellerRepository;
import com.example.pharmaaggregatorserver.service.product.ProductDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductDetailsServiceImpl implements ProductDetailsService {

    private final ProductDetailsRepository productRepo;
    private final CategoryRepository categoryRepo;
    private final SellerRepository sellerRepo;
    private final MoleculeRepository moleculeRepo;
    private final ProductDetailsMapper productMapper;
    private final PackagingDetailsRepository packagingDetailsRepository;
    private final PricingDetailsRepository pricingDetailsRepository;


//    public ProductDetailsDto createProduct(ProductDetailsDto dto, Long userId) {
//
//        Seller seller = sellerRepo.findByUserId(userId)
//                .orElseThrow(() -> new RuntimeException("Seller not found"));
//
////        log.info("Seller Id is: " + seller.getSellerId());
//
//        Category category = categoryRepo.findById(dto.getCategoryId())
//                .orElseThrow(() -> new RuntimeException("Category not found"));
//
//        ProductDetails product = productMapper.toEntity(dto);
//
//        product.setProductId(
//                generateProductId(dto.getProductName(), seller.getSellerName())
//        );
//        product.setCreatedBy(seller.getSellerId());
//        product.setSeller(seller);
//        product.setCategory(category);
//
//        setChildRelationships(product, seller.getSellerName(), seller.getSellerId());
//
//        ProductDetails saved = productRepo.save(product);
//
//        if (dto.getMolecules() != null) {
//            Set<Molecule> molecules = dto.getMolecules().stream()
//                    .map(moleculeDto -> moleculeRepo.findById(moleculeDto.getMoleculeId())
//                            .orElseThrow(() -> new RuntimeException(
//                                    "Molecule not found: " + moleculeDto.getMoleculeId())))
//                    .collect(Collectors.toSet());
//
//            saved.setMolecules(molecules);
//        }
//
//        ProductDetails finalSaved = productRepo.save(saved);
//
//        return productMapper.toDto(finalSaved);
//    }

    @Override
    @Transactional
    public ProductDetailsDto createProduct(ProductDetailsDto dto, Long userId) {

        Seller seller = sellerRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        Category category = categoryRepo.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        ProductDetails product = productMapper.toEntity(dto);

        product.setProductId(
                generateProductId(dto.getProductName(), seller.getSellerName())
        );
        product.setCreatedBy(seller.getSellerId());
        product.setCreatedDate(LocalDateTime.now());
        product.setSeller(seller);
        product.setCategory(category);

        setChildRelationships(product, seller.getSellerName(), seller.getSellerId());

        ProductDetails saved = productRepo.saveAndFlush(product);

        if (dto.getMolecules() != null && !dto.getMolecules().isEmpty()) {

            Set<Molecule> molecules = dto.getMolecules().stream()
                    .map(moleculeDto -> moleculeRepo.findById(moleculeDto.getMoleculeId())
                            .orElseThrow(() -> new RuntimeException(
                                    "Molecule not found: " + moleculeDto.getMoleculeId())))
                    .collect(Collectors.toSet());

            saved.setMolecules(molecules);

            saved = productRepo.save(saved);
        }

        return productMapper.toDto(saved);
    }

    private void setChildRelationships(ProductDetails product, String sellerName, String sellerId) {

        if (product.getPackagingDetails() != null) {
            product.getPackagingDetails().setPackagingId(
                    generatePackagingId(sellerName)
            );
            product.getPackagingDetails().setProductDetails(product);
            product.getPackagingDetails().setCreatedBy(sellerId);
            product.getPackagingDetails().setCreatedDate(LocalDateTime.now());
        }

        if (product.getPricingDetails() != null) {
            product.getPricingDetails().forEach(p -> {
                p.setPricingId(generatePricingId(sellerName));
                p.setProductDetails(product);
                p.setCreatedBy(sellerId);
                p.setCreatedDate(LocalDateTime.now());
            });
        }


        if (product.getProductAttributeDrugs() != null) {
            product.getProductAttributeDrugs().forEach(a -> {
                a.setProductAttributeId(UUID.randomUUID().toString());
                a.setProductDetails(product);
                a.setCreatedBy(sellerId);
                a.setCreatedDate(LocalDateTime.now());
            });
        }

        if (product.getProductImages() != null) {
            product.getProductImages().forEach(img -> {
                img.setProductImageId(UUID.randomUUID().toString());
                img.setProductDetails(product);
            });
        }
    }


    // Product ID generation
    private synchronized String generateProductId(String productName, String sellerName) {

        String cleanedSeller = sellerName
                .replaceAll("[^a-zA-Z]", "")
                .toUpperCase();

        String prefix;
        if (cleanedSeller.length() >= 2) {
            prefix = cleanedSeller.substring(0, 2);
        } else {
            prefix = String.format("%-2s", cleanedSeller).replace(' ', 'X');
        }

        String namePart = productName
                .replaceAll("[^a-zA-Z]", "")
                .toUpperCase();

        if (namePart.length() >= 3) {
            namePart = namePart.substring(0, 3);
        } else {
            namePart = String.format("%-3s", namePart).replace(' ', 'X');
        }
        Integer lastNumber = productRepo.findMaxProductNumber();
        int nextNumber = (lastNumber == null) ? 1 : lastNumber + 1;

        String formattedNumber = String.format("%05d", nextNumber);

        return prefix + namePart + formattedNumber;
    }


    // Packaging ID generation
    private synchronized String generatePackagingId(String sellerName) {

        String cleanedSeller = sellerName
                .replaceAll("[^a-zA-Z]", "")
                .toUpperCase();

        String prefix;
        if (cleanedSeller.length() >= 2) {
            prefix = cleanedSeller.substring(0, 2);
        } else {
            prefix = String.format("%-2s", cleanedSeller).replace(' ', 'X');
        }

        String prefixNew = "PKG";

        Integer lastNumber = packagingDetailsRepository.findMaxPackagingNumber();
        int nextNumber = (lastNumber == null) ? 1 : lastNumber + 1;

        String formattedNumber = String.format("%05d", nextNumber);

        return prefix + prefixNew + formattedNumber;
    }


    // Pricing ID generation
    private synchronized String generatePricingId(String sellerName) {

        String cleanedSeller = sellerName
                .replaceAll("[^a-zA-Z]", "")
                .toUpperCase();

        String prefix;
        if (cleanedSeller.length() >= 2) {
            prefix = cleanedSeller.substring(0, 2);
        } else {
            prefix = String.format("%-2s", cleanedSeller).replace(' ', 'X');
        }

        String prefixNew = "BTCH";

        Integer lastNumber = pricingDetailsRepository.findMaxPricingNumber();
        int nextNumber = (lastNumber == null) ? 1 : lastNumber + 1;

        return prefix + prefixNew + String.format("%05d", nextNumber);
    }
}