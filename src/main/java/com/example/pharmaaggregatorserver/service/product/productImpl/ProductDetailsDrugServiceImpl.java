package com.example.pharmaaggregatorserver.service.product.productImpl;

import com.example.pharmaaggregatorserver.dto.product.*;
import com.example.pharmaaggregatorserver.entity.product.Molecule;
import com.example.pharmaaggregatorserver.entity.product.PackagingDetailsDrug;
import com.example.pharmaaggregatorserver.entity.product.PricingDetailsDrug;
import com.example.pharmaaggregatorserver.entity.product.ProductDetailsDrug;
import com.example.pharmaaggregatorserver.mapper.product.ProductDetailsDrugMapper;
import com.example.pharmaaggregatorserver.repository.product.MoleculeRepository;
import com.example.pharmaaggregatorserver.repository.product.PackagingDetailsDrugRepository;
import com.example.pharmaaggregatorserver.repository.product.PricingDetailsDrugRepository;
import com.example.pharmaaggregatorserver.repository.product.ProductDetailsDrugRepository;
import com.example.pharmaaggregatorserver.service.product.ProductDetailsDrugService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductDetailsDrugServiceImpl implements ProductDetailsDrugService {

    private final ProductDetailsDrugRepository productRepository;
    private final MoleculeRepository moleculeRepository;
    private final PricingDetailsDrugRepository pricingRepository;
    private final PackagingDetailsDrugRepository packagingRepository;

    @Override
    @Transactional
    public ProductDetailsDrugDto createProduct(CreateProductRequestDto request) {

        // 1️⃣ Product
        ProductDetailsDrug product =
                ProductDetailsDrugMapper.toEntity(request.getProduct());

        product.setProductId(generateProductId(product.getProductName()));

        // 2️⃣ Molecules (MANY-TO-MANY)
        if (request.getMoleculeIds() != null && !request.getMoleculeIds().isEmpty()) {

            Set<Molecule> molecules =
                    moleculeRepository.findAllById(request.getMoleculeIds())
                            .stream()
                            .collect(Collectors.toSet());

            if (molecules.size() != request.getMoleculeIds().size()) {
                throw new RuntimeException("One or more molecules not found");
            }

            product.setMolecules(molecules);
        }

        // 3️⃣ Packaging (ONE-TO-ONE)
        if (request.getPackagingDetails() != null) {

            PackagingDetailsDrug packaging =
                    ProductDetailsDrugMapper.toPackagingEntity(
                            request.getPackagingDetails());

            packaging.setPackagingId(generatePackagingId());
            packaging.setProduct(product);
            product.setPackagingDetails(packaging);
        }

        // 4️⃣ Pricing (ONE-TO-MANY)
        if (request.getPricingDetails() != null &&
                !request.getPricingDetails().isEmpty()) {

            Set<PricingDetailsDrug> pricingEntities =
                    request.getPricingDetails().stream()
                            .map(ProductDetailsDrugMapper::toPricingEntity)
                            .collect(Collectors.toSet());

            pricingEntities.forEach(pricing -> {
                pricing.setPricingId(generatePricingId());
                pricing.setProduct(product);
            });

            product.setPricingDetails(pricingEntities);
        }

        // 5️⃣ SAVE (transactional)
        ProductDetailsDrug saved = productRepository.save(product);

        return ProductDetailsDrugMapper.toDto(saved);
    }


    @Override
    @Transactional(readOnly = true)
    public List<ProductDetailsDrugDto> getAllProducts() {

        return productRepository.findAll()
                .stream()
                .map(ProductDetailsDrugMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailsDrugDto getProductById(String productId) {

        ProductDetailsDrug product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Product not found with id: " + productId));

        return ProductDetailsDrugMapper.toDto(product);
    }

    @Override
    public void deleteProduct(String productId) {

        ProductDetailsDrug product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Product not found with id: " + productId));
        productRepository.delete(product);
    }


    @Override
    public ProductDetailsDrug updateProduct(
            String productId,
            ProductDetailsDrugDto dto
    ) {

        ProductDetailsDrug existingProduct =
                productRepository.findById(productId)
                        .orElseThrow(() -> new RuntimeException("Product not found"));

        // Product feilds updation
        existingProduct.setProductName(dto.getProductName());
        existingProduct.setTherapeuticCategory(dto.getTherapeuticCategory());
        existingProduct.setTherapeuticSubcategory(dto.getTherapeuticSubcategory());
        existingProduct.setDosageForm(dto.getDosageForm());
        existingProduct.setStrength(dto.getStrength());
        existingProduct.setWarningsPrecautions(dto.getWarningsPrecautions());
        existingProduct.setProductDescription(dto.getProductDescription());
        existingProduct.setProductImage(dto.getProductImage());
        existingProduct.setProductMarketingUrl(dto.getProductMarketingUrl());

        // Packging feilds updation
        if (existingProduct.getPackagingDetails() != null &&
                dto.getPackagingDetails() != null) {

            PackagingDetailsDrugDto pDto = dto.getPackagingDetails();
            PackagingDetailsDrug packaging =
                    existingProduct.getPackagingDetails();

            packaging.setPackagingUnit(pDto.getPackagingUnit());
            packaging.setNumberOfUnits(pDto.getNumberOfUnits());
            packaging.setPackSize(pDto.getPackSize());
            packaging.setMinimumOrderQuantity(pDto.getMinimumOrderQuantity());
            packaging.setMaximumOrderQuantity(pDto.getMaximumOrderQuantity());
        }

        // Adding new pricing row
//        if (dto.getPricingDetails() != null) {
//            for (PricingDetailsDrugDto priceDto : dto.getPricingDetails()) {
//
//                if (priceDto.getPricingId() != null) continue;
//
//                PricingDetailsDrug pricing =
//                        ProductDetailsDrugMapper.toPricingEntity(priceDto);
//
//                pricing.setPricingId(null);
//                pricing.setProduct(existingProduct);
//
//                pricingRepository.save(pricing);
//            }
//        }

        // Pricing fields updation / insertion
        if (dto.getPricingDetails() != null) {

            for (PricingDetailsDrugDto priceDto : dto.getPricingDetails()) {

                // 🔹 UPDATE existing pricing
                if (priceDto.getPricingId() != null) {

                    PricingDetailsDrug existingPricing =
                            pricingRepository.findById(priceDto.getPricingId())
                                    .orElseThrow(() ->
                                            new RuntimeException("Pricing not found"));

                    existingPricing.setBatchLotNumber(priceDto.getBatchLotNumber());
                    existingPricing.setManufacturerName(priceDto.getManufacturerName());
                    existingPricing.setManufacturingDate(priceDto.getManufacturingDate());
                    existingPricing.setExpiryDate(priceDto.getExpiryDate());
                    existingPricing.setStorageCondition(priceDto.getStorageCondition());
                    existingPricing.setStockQuantity(priceDto.getStockQuantity());
                    existingPricing.setPricePerUnit(priceDto.getPricePerUnit());
                    existingPricing.setMrp(priceDto.getMrp());
                    existingPricing.setDiscountPercentage(priceDto.getDiscountPercentage());
                    existingPricing.setGstPercentage(priceDto.getGstPercentage());
                    existingPricing.setHsnCode(priceDto.getHsnCode());

                    pricingRepository.save(existingPricing);
                }

                // 🔹 CREATE new pricing
                else {
                    PricingDetailsDrug pricing =
                            ProductDetailsDrugMapper.toPricingEntity(priceDto);

                    pricing.setProduct(existingProduct);
                    pricingRepository.save(pricing);
                }
            }
        }
        return productRepository.save(existingProduct);
    }

    // Product ID generation
    private synchronized String generateProductId(String productName) {

        String prefix = "SN";

        String namePart = productName
                .replaceAll("[^a-zA-Z]", "")
                .toUpperCase();

        if (namePart.length() >= 3) {
            namePart = namePart.substring(0, 3);
        } else {
            namePart = String.format("%-3s", namePart).replace(' ', 'X');
        }
        Integer lastNumber = productRepository.findMaxProductNumber();
        int nextNumber = (lastNumber == null) ? 1 : lastNumber + 1;

        String formattedNumber = String.format("%05d", nextNumber);

        return prefix + namePart + formattedNumber;
    }


    // Packaging ID generation
    private synchronized String generatePackagingId() {

        String prefix = "SNPKG";

        Integer lastNumber = packagingRepository.findMaxPackagingNumber();
        int nextNumber = (lastNumber == null) ? 1 : lastNumber + 1;

        String formattedNumber = String.format("%05d", nextNumber);

        return prefix + formattedNumber;
    }


    // Pricing ID generation
    private synchronized String generatePricingId() {

        String prefix = "SNBTCH";

        Integer lastNumber = pricingRepository.findMaxPricingNumber();
        int nextNumber = (lastNumber == null) ? 1 : lastNumber + 1;

        return prefix + String.format("%05d", nextNumber);
    }


    @Override
    public List<TherapeuticSubcategoryDto> getSubcategories(String categoryId) {
        return productRepository.findByCategoryId(categoryId);

    }
}