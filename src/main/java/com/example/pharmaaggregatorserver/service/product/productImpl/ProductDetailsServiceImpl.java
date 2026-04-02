package com.example.pharmaaggregatorserver.service.product.productImpl;

import com.example.pharmaaggregatorserver.dto.product.*;
import com.example.pharmaaggregatorserver.entity.product.*;
import com.example.pharmaaggregatorserver.entity.seller.Seller;
import com.example.pharmaaggregatorserver.mapper.product.*;
import com.example.pharmaaggregatorserver.repository.product.*;
import com.example.pharmaaggregatorserver.repository.seller.SellerRepository;
import com.example.pharmaaggregatorserver.service.product.ProductDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
    private final PackagingDetailsMapper packagingDetailsMapper;
    private final PricingDetailsMapper pricingDetailsMapper;
    private final ProductAttributeDrugMapper productAttributeDrugMapper;
    private final ProductImageMapper productImageMapper;
    private final PackTypeRepository packTypeRepository;
    private final MoleculeRepository moleculeRepository;


    @Override
    @Transactional
    public ProductDetailsDto createProduct(ProductDetailsDto dto, Long userId) {

        Seller seller = sellerRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        Category category = categoryRepo.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        ProductDetails product = productMapper.toEntity(dto);

        product.setProductImages(null);

        product.setProductId(
                generateProductId(dto.getProductName(), seller.getSellerName())
        );
        product.setCreatedBy(seller.getSellerId());
        product.setCreatedDate(LocalDateTime.now());
        product.setSeller(seller);
        product.setCategory(category);

        setChildRelationships(product, seller.getSellerName(), seller.getSellerId());

        ProductDetails saved = productRepo.save(product);


        return productMapper.toDto(saved);
    }

    private void setChildRelationships(ProductDetails product, String sellerName, String sellerId) {

        if (product.getPackagingDetails() != null) {

            PackagingDetails packaging = product.getPackagingDetails();

            packaging.setPackagingId(generatePackagingId(sellerName));
            packaging.setProductDetails(product);
            packaging.setCreatedBy(sellerId);
            packaging.setCreatedDate(LocalDateTime.now());

            if (packaging.getPackType() == null || packaging.getPackType().getPackId() == null) {
                throw new RuntimeException("PackType (packId) is required");
            }

            PackType packType = packTypeRepository.findById(packaging.getPackType().getPackId())
                    .orElseThrow(() -> new RuntimeException("Invalid packId"));

            packaging.setPackType(packType);
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

                if (a.getProductMolecules() != null) {

                    a.getProductMolecules().forEach(pm -> {

                        Molecule molecule = moleculeRepository.findById(
                                pm.getMolecule().getMoleculeId()
                        ).orElseThrow(() -> new RuntimeException("Molecule not found"));

                        pm.setProductAttributeDrug(a);
                        pm.setMolecule(molecule);

                        ProductMoleculeId id = new ProductMoleculeId(
                                a.getProductAttributeId(),
                                molecule.getMoleculeId()
                        );

                        pm.setId(id);
                    });
                }
            });
        }

        if (product.getProductAttributeNonConsumableMedicals() != null) {
            product.getProductAttributeNonConsumableMedicals().forEach(a -> {

                // Validate mandatory FK fields are resolved
                if (a.getDeviceCategory() == null) {
                    throw new RuntimeException("deviceCategoryId is required for non-consumable medical attribute");
                }
                if (a.getCertification() == null) {
                    throw new RuntimeException("certificationId is required for non-consumable medical attribute");
                }

                a.setProductAttributeId(UUID.randomUUID().toString());
                a.setProductDetails(product);
                a.setCreatedBy(sellerId);
                a.setCreatedDate(LocalDateTime.now());
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

    @Override
    @Transactional(readOnly = true)
    public List<ProductDetailsDto> getAllProducts(Long userId) {

        Seller seller = sellerRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        List<ProductDetails> products = productRepo.findBySellerSellerId(seller.getSellerId());

        return products.stream()
                .map(productMapper::toDto)
                .toList();
    }


    @Override
    @Transactional(readOnly = true)
    public ProductDetailsDto getProductById(String productId, Long userId) {

        Seller seller = sellerRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        ProductDetails product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!product.getSeller().getSellerId().equals(seller.getSellerId())) {
            throw new RuntimeException("Unauthorized access to this product");
        }

        return productMapper.toDto(product);
    }


    @Override
    @Transactional
    public void deleteProductById(String productId, Long userId) {

        Seller seller = sellerRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        ProductDetails product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!product.getSeller().getSellerId().equals(seller.getSellerId())) {
            throw new RuntimeException("Unauthorized to delete this product");
        }

        productRepo.delete(product);
    }

    @Override
    @Transactional
    public ProductDetailsDto updateProduct(String productId, ProductDetailsDto dto, Long userId) {

        Seller seller = sellerRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        ProductDetails existingProduct = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!existingProduct.getSeller().getSellerId().equals(seller.getSellerId())) {
            throw new RuntimeException("Unauthorized to update this product");
        }


        existingProduct.setProductName(dto.getProductName());
        existingProduct.setWarningsPrecautions(dto.getWarningsPrecautions());
        existingProduct.setProductDescription(dto.getProductDescription());
        existingProduct.setProductMarketingUrl(dto.getProductMarketingUrl());
        existingProduct.setModifiedBy(seller.getSellerId());
        existingProduct.setModifiedDate(LocalDateTime.now());

        if (dto.getCategoryId() != null) {
            Category category = categoryRepo.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            existingProduct.setCategory(category);
        }


        if (dto.getPackagingDetails() != null) {

            PackagingDetails existingPackaging = existingProduct.getPackagingDetails();

            if (existingPackaging == null) {
                PackagingDetails newPackaging =
                        packagingDetailsMapper.toEntity(dto.getPackagingDetails());

                newPackaging.setProductDetails(existingProduct);
                existingProduct.setPackagingDetails(newPackaging);

            } else {
                PackagingDetailsDto pd = dto.getPackagingDetails();

                existingPackaging.setUnitPerPack(pd.getUnitPerPack());
                existingPackaging.setNumberOfPacks(pd.getNumberOfPacks());
                existingPackaging.setPackSize(pd.getPackSize());
                existingPackaging.setMinimumOrderQuantity(pd.getMinimumOrderQuantity());
                existingPackaging.setMaximumOrderQuantity(pd.getMaximumOrderQuantity());
                existingPackaging.setModifiedBy(seller.getSellerId());
                existingPackaging.setModifiedDate(LocalDateTime.now());
            }
        }


        if (dto.getPricingDetails() != null) {

            Set<PricingDetails> existingPricing = existingProduct.getPricingDetails();

            Map<String, PricingDetails> existingMap = existingPricing.stream()
                    .collect(Collectors.toMap(PricingDetails::getPricingId, p -> p));

            for (PricingDetailsDto dtoPricing : dto.getPricingDetails()) {

                if (dtoPricing.getPricingId() != null &&
                        existingMap.containsKey(dtoPricing.getPricingId())) {

                    PricingDetails existing = existingMap.get(dtoPricing.getPricingId());

                    existing.setBatchLotNumber(dtoPricing.getBatchLotNumber());
//                    existing.setManufacturerName(dtoPricing.getManufacturerName());
                    existing.setManufacturingDate(dtoPricing.getManufacturingDate());
                    existing.setExpiryDate(dtoPricing.getExpiryDate());
                    existing.setStorageCondition(dtoPricing.getStorageCondition());
                    existing.setStockQuantity(dtoPricing.getStockQuantity());
                    existing.setDateOfStockEntry(dtoPricing.getDateOfStockEntry());
                    existing.setSellingPrice(dtoPricing.getSellingPrice());
                    existing.setMrp(dtoPricing.getMrp());
                    existing.setDiscountPercentage(dtoPricing.getDiscountPercentage());
                    existing.setGstPercentage(dtoPricing.getGstPercentage());
//                    existing.setMinimumPurchaseQuantity(dtoPricing.getMinimumPurchaseQuantity());
//                    existing.setAdditionalDiscount(dtoPricing.getAdditionalDiscount());
                    existing.setFinalPrice(dtoPricing.getFinalPrice());
                    existing.setHsnCode(dtoPricing.getHsnCode());
                    existing.setShelfLifeMonths(dtoPricing.getShelfLifeMonths());

                    existing.setModifiedBy(seller.getSellerId());
                    existing.setModifiedDate(LocalDateTime.now());

                } else {

                    PricingDetails newPricing = pricingDetailsMapper.toEntity(dtoPricing);

                    newPricing.setPricingId(generatePricingId(seller.getSellerName()));
                    newPricing.setCreatedBy(seller.getSellerId());
                    newPricing.setCreatedDate(LocalDateTime.now());

                    newPricing.setProductDetails(existingProduct);

                    existingPricing.add(newPricing);
                }
            }
        }


        if (dto.getProductAttributeDrugs() != null) {

            Set<ProductAttributeDrug> existingAttrs = existingProduct.getProductAttributeDrugs();

            Map<String, ProductAttributeDrug> existingMap = existingAttrs.stream()
                    .collect(Collectors.toMap(ProductAttributeDrug::getProductAttributeId, a -> a));

            for (ProductAttributeDrugDto dtoAttr : dto.getProductAttributeDrugs()) {

                if (dtoAttr.getProductAttributeId() != null &&
                        existingMap.containsKey(dtoAttr.getProductAttributeId())) {

                    ProductAttributeDrug existingAttr =
                            existingMap.get(dtoAttr.getProductAttributeId());

                    existingAttr.setTherapeuticCategoryId(dtoAttr.getTherapeuticCategoryId());
                    existingAttr.setTherapeuticSubcategoryId(dtoAttr.getTherapeuticSubcategoryId());
                    existingAttr.setDosageForm(dtoAttr.getDosageForm());
                    existingAttr.setModifiedBy(seller.getSellerId());
                    existingAttr.setModifiedDate(LocalDateTime.now());

                }
            }
        }


//        if (dto.getProductImages() != null) {
//
//            existingProduct.getProductImages().clear();
//
//            Set<ProductImage> images = dto.getProductImages().stream()
//                    .map(img -> {
//                        ProductImage image = productImageMapper.toEntity(img);
//                        image.setProductDetails(existingProduct);
//                        return image;
//                    })
//                    .collect(Collectors.toSet());
//
//            existingProduct.getProductImages().addAll(images);
//        }

        ProductDetails updatedProduct = productRepo.save(existingProduct);

        return productMapper.toDto(updatedProduct);
    }

    @Override
    public List<TherapeuticSubcategoryDto> getSubcategories(String categoryId) {
        return productRepo.findByCategoryId(categoryId);

    }

}