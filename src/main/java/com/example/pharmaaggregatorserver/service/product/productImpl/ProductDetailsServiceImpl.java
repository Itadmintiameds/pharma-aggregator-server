package com.example.pharmaaggregatorserver.service.product.productImpl;

import com.example.pharmaaggregatorserver.dto.product.*;
import com.example.pharmaaggregatorserver.entity.product.*;
import com.example.pharmaaggregatorserver.entity.product.CosmeticPersonalCareMasters.IntendedUseArea;
import com.example.pharmaaggregatorserver.entity.product.MedicalDeviceProductMaster.Certification;
import com.example.pharmaaggregatorserver.entity.product.MedicalDeviceProductMaster.StorageConditionMaster;
import com.example.pharmaaggregatorserver.entity.seller.Seller;
import com.example.pharmaaggregatorserver.exception.ApplicationException;
import com.example.pharmaaggregatorserver.exception.NotFoundException;
import com.example.pharmaaggregatorserver.mapper.product.*;
import com.example.pharmaaggregatorserver.repository.product.*;
import com.example.pharmaaggregatorserver.repository.seller.SellerRepository;
import com.example.pharmaaggregatorserver.service.S3Service;
import com.example.pharmaaggregatorserver.service.product.PackagingDetailsService;
import com.example.pharmaaggregatorserver.service.product.PricingDetailsService;
import com.example.pharmaaggregatorserver.service.product.ProductDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductDetailsServiceImpl implements ProductDetailsService {
    private final DeviceCategoryRepository DeviceCategoryRepository;
    private final ProductDetailsRepository productRepo;
    private final CategoryRepository categoryRepo;
    private final SellerRepository sellerRepo;
    private final MoleculeRepository moleculeRepo;
    private final ProductDetailsMapper productMapper;
    private final PackagingDetailsRepository packagingDetailsRepository;
    private final PackagingDetailsMapper packagingDetailsMapper;
    private final PricingDetailsMapper pricingDetailsMapper;
    private final ProductAttributeDrugMapper productAttributeDrugMapper;
    private final ProductImageMapper productImageMapper;
    private final PackTypeRepository packTypeRepository;
    private final MoleculeRepository moleculeRepository;
    private final AdditionalDiscountMapper additionalDiscountMapper;
    private final StorageConditionMasterRepository storageConditionMasterRepository;
    private final ProductAttributeFoodInfantMapper productAttributeFoodInfantMapper;
    private final intendedUseAreaRepository intendedUseAreaRepository;
    private final FlavourRepository flavourRepository;
    private final CertificationRepository certificationRepository;
    private final S3Service s3Service;
    private final ProductImageService productImageService;
    private final PricingDetailsService pricingDetailsService;
    private final PackagingDetailsService packagingDetailsService;


    @Override
    @Transactional
    public ProductDetailsDto createProduct(ProductDetailsDto dto, Long userId, boolean allowMergeIntoExisting) {

        Seller seller = sellerRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        Category category = categoryRepo.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        String productName = dto.getProductName() != null ? dto.getProductName().trim() : null;
        String manufacturerName = dto.getManufacturerName() != null ? dto.getManufacturerName().trim() : null;

        if (allowMergeIntoExisting) {
            // Same seller + name + manufacturer + category means this is another variant
            // (pack size/batch) of a product that already exists, not a brand-new product.
            Optional<ProductDetails> existingOpt = productRepo
                    .findFirstBySeller_SellerIdAndProductNameIgnoreCaseAndManufacturerNameIgnoreCaseAndCategory_CategoryId(
                            seller.getSellerId(), productName, manufacturerName, dto.getCategoryId());

            if (existingOpt.isPresent()) {
                return addVariantToExistingProduct(existingOpt.get(), dto, seller);
            }
        }

        ProductDetails product = productMapper.toEntity(dto);


        product.setProductImages(null);

        product.setProductId(
                generateProductId(productName, seller.getSellerName())
        );
        product.setProductName(productName);
        product.setManufacturerName(manufacturerName);
        product.setCreatedBy(seller.getSellerId());
        product.setCreatedDate(LocalDateTime.now());
        product.setSeller(seller);
        product.setCategory(category);
        product.setGstPercentage(dto.getGstPercentage());
        product.setHsnCode(dto.getHsnCode());

        setChildRelationships(product, seller.getSellerName(), seller.getSellerId());

        ProductDetails saved = productRepo.save(product);


        return productMapper.toDto(saved);
    }

    // Adds the incoming packaging/pricing (a new pack-size variant and/or batch) onto a
    // product that already exists for this seller, instead of creating a duplicate product.
    // Other product-level fields (description, attributes, etc.) are intentionally left
    // untouched here — use updateProduct if those need to change.
    private ProductDetailsDto addVariantToExistingProduct(ProductDetails existingProduct, ProductDetailsDto dto, Seller seller) {

        if (dto.getGstPercentage() != null) {
            existingProduct.setGstPercentage(dto.getGstPercentage());
        }
        if (dto.getHsnCode() != null) {
            existingProduct.setHsnCode(dto.getHsnCode());
        }

        PackagingDetails singleResolvedPackaging = null;

        if (dto.getPackagingDetails() != null) {
            if (existingProduct.getPackagingDetails() == null) {
                existingProduct.setPackagingDetails(new HashSet<>());
            }
            for (PackagingDetailsDto pd : dto.getPackagingDetails()) {
                PackagingDetails candidate = packagingDetailsMapper.toEntity(pd);
                PackagingDetails resolved = packagingDetailsService.resolveOrCreatePackaging(
                        existingProduct, candidate, seller.getSellerName(), seller.getSellerId());
                existingProduct.getPackagingDetails().add(resolved);
                singleResolvedPackaging = resolved;
            }
        }

        if (dto.getPricingDetails() != null) {
            if (existingProduct.getPricingDetails() == null) {
                existingProduct.setPricingDetails(new HashSet<>());
            }
            for (PricingDetailsDto pDto : dto.getPricingDetails()) {
                PricingDetails candidate = pricingDetailsMapper.toEntity(pDto);
                PackagingDetails linkTo = resolvePackagingLink(existingProduct, pDto, singleResolvedPackaging);
                PricingDetails pricing = pricingDetailsService.resolveOrCreateBatch(
                        existingProduct, linkTo, candidate, seller.getSellerName(), seller.getSellerId());
                existingProduct.getPricingDetails().add(pricing);
            }
        }

        ProductDetails saved = productRepo.save(existingProduct);
        return productMapper.toDto(saved);
    }

    // Resolves which packaging variant a pricing/batch entry belongs to: an explicit
    // packagingId on the DTO wins (validated to actually belong to this product); otherwise,
    // if this call resolved exactly one packaging entry, pair with it (matches the "1 packaging
    // + 1 pricing per row" convention every import strategy already follows); otherwise null.
    private PackagingDetails resolvePackagingLink(
            ProductDetails product, PricingDetailsDto prDto, PackagingDetails singleResolvedPackaging) {

        if (prDto.getPackagingId() != null && !prDto.getPackagingId().isBlank()) {
            return packagingDetailsRepository.findById(prDto.getPackagingId())
                    .filter(p -> p.getProductDetails().getProductId().equals(product.getProductId()))
                    .orElseThrow(() -> new NotFoundException(
                            "Packaging " + prDto.getPackagingId() + " not found on this product"));
        }
        return singleResolvedPackaging;
    }

    public List<ProductDetailsDto> getAllProductsForAdmin() {
        return productRepo.findAll()
                .stream()
                .map(productMapper::toDto)
                .toList();
    }

    private void setChildRelationships(ProductDetails product, String sellerName, String sellerId) {

        // Product isn't persisted yet, so these lookups will always find nothing and fall
        // through to "create new" — routed through the same resolve helpers as the
        // reuse-existing-product path anyway, so there's exactly one implementation of
        // "dedupe packaging"/"restock-or-create batch" for both code paths to stay in sync.
        PackagingDetails singleResolvedPackaging = null;

        if (product.getPackagingDetails() != null) {
            Set<PackagingDetails> resolvedPackaging = new HashSet<>();
            for (PackagingDetails p : product.getPackagingDetails()) {
                PackagingDetails resolved = packagingDetailsService.resolveOrCreatePackaging(product, p, sellerName, sellerId);
                resolvedPackaging.add(resolved);
                singleResolvedPackaging = resolved;
            }
            product.setPackagingDetails(resolvedPackaging);
        }

        if (product.getPricingDetails() != null) {
            Set<PricingDetails> resolvedPricing = new HashSet<>();
            for (PricingDetails p : product.getPricingDetails()) {
                PricingDetails resolved = pricingDetailsService.resolveOrCreateBatch(
                        product, singleResolvedPackaging, p, sellerName, sellerId);
                resolvedPricing.add(resolved);
            }
            product.setPricingDetails(resolvedPricing);
        }

        if (product.getProductAttributeDrugs() != null) {
            product.getProductAttributeDrugs().forEach(a -> {

                a.setProductDetails(product);
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

                    });
                }
            });
        }


// ================= Non CONSUMABLE MEDICAL =================

        if (product.getProductAttributeNonConsumableMedicals() != null) {
            product.getProductAttributeNonConsumableMedicals().forEach(a -> {

                if (a.getDeviceCategory() == null) {
                    throw new RuntimeException("deviceCategoryId is required for non-consumable medical attribute");
                }
                if (a.getCertifications() == null || a.getCertifications().isEmpty()) {
                    throw new RuntimeException("At least one certification is required for non-consumable medical attribute");
                }

                a.setProductAttributeId(UUID.randomUUID().toString());
                a.setProductDetails(product);
                a.setCreatedBy(sellerId);
                a.setCreatedDate(LocalDateTime.now());

                // Bind each certificate document back to the now-ID'd entity
                if (a.getCertificateDocuments() != null) {
                    a.getCertificateDocuments().forEach(doc -> {
                        doc.setNonConsumableMedical(a);
                        doc.setCreatedBy(sellerId);
                        doc.setCreatedDate(LocalDateTime.now());
                    });
                }
            });
        }
        // ================= CONSUMABLE MEDICAL =================
        if (product.getProductAttributeConsumables() != null) {
            product.getProductAttributeConsumables().forEach(a -> {

                if (a.getDeviceCategory() == null) {
                    throw new RuntimeException("deviceCategoryId is required for consumable medical attribute");
                }

                if (a.getCertifications() == null) {
                    throw new RuntimeException("At least one certification is required");
                }

                a.setProductAttributeId(UUID.randomUUID().toString());
                a.setProductDetails(product);
                a.setCreatedBy(sellerId);
                a.setCreatedDate(LocalDateTime.now());

                // Bind certificate documents back to the entity
                if (a.getCertificateDocuments() != null) {
                    a.getCertificateDocuments().forEach(doc -> {
                        doc.setConsumableMedical(a);
                        doc.setCreatedBy(sellerId);
                        doc.setCreatedDate(LocalDateTime.now());
                    });
                }
            });
        }
        // ================= Cosmetic and Personal use =================
        if (product.getProductAttributeCosmeticandPersonalCare() != null) {
            product.getProductAttributeCosmeticandPersonalCare().forEach(a -> {

//                if (a.getDeviceCategory() == null) {
//                    throw new RuntimeException("deviceCategoryId is required for consumable medical attribute");
//                }

                if (a.getCertifications() == null) {
                    throw new RuntimeException("At least one certification is required");
                }

                a.setProductAttributeId(UUID.randomUUID().toString());
                a.setProductDetails(product);
                a.setCreatedBy(sellerId);
                a.setCreatedDate(LocalDateTime.now());

                // Bind certificate documents back to the entity
                if (a.getCertificateDocuments() != null) {
                    a.getCertificateDocuments().forEach(doc -> {
                        doc.setCosmeticAndPersonalUse(a);
                        doc.setCreatedBy(sellerId);
                        doc.setCreatedDate(LocalDateTime.now());
                    });
                }
            });
        }

        // ================= Supplements/ Nutraceuticals =================

        if (product.getProductAttributeSupplementsOrNutraceuticals() != null) {
            product.getProductAttributeSupplementsOrNutraceuticals().forEach(a -> {

                if (a.getTherapeuticCategory() == null) {
                    throw new RuntimeException("Therapeutic Category is required");
                }
                if (a.getCertifications() == null || a.getCertifications().isEmpty()) {
                    throw new RuntimeException("At least one certification is required");
                }

                a.setProductAttributeId(UUID.randomUUID().toString());
                a.setProductDetails(product);
                a.setCreatedBy(sellerId);
                a.setCreatedDate(LocalDateTime.now());

                // Bind each certificate document back to the now-ID'd entity
                if (a.getCertificateDocuments() != null) {
                    a.getCertificateDocuments().forEach(doc -> {
                        doc.setSupplementsOrNutraceuticals(a);
                        doc.setCreatedBy(sellerId);
                        doc.setCreatedDate(LocalDateTime.now());
                    });
                }
            });
        }


        // ================= FOOD INFANT =================

        if (product.getProductAttributeFoodInfants() != null) {
            product.getProductAttributeFoodInfants().forEach(a -> {

                // ✅ Basic validation (optional but recommended)
                if (a.getProductCategoryMaster() == null) {
                    throw new RuntimeException("Product Category is required for food infant");
                }

                if (a.getCertifications() == null || a.getCertifications().isEmpty()) {
                    throw new RuntimeException("At least one certification is required for food infant");
                }

                a.setProductDetails(product);
                //a.setProductAttributeId(UUID.randomUUID().toString());
                a.setCreatedBy(sellerId);
                a.setCreatedDate(LocalDateTime.now());
                if (a.getCertificateDocuments() != null) {
                    a.getCertificateDocuments().forEach(doc -> {

                        doc.setProductAttributeFoodInfant(a);
                        doc.setCreatedBy(sellerId);
                        doc.setCreatedDate(LocalDateTime.now());

                    });
                }
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


    // Packaging IDs are now generated exclusively by PackagingDetailsServiceImpl,
    // since resolveOrCreatePackaging() there is the single place variants are actually created.
    // Pricing/batch IDs are likewise generated exclusively by PricingDetailsServiceImpl,
    // since resolveOrCreateBatch() there is the single place batches are actually created.

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
    public PackagingDetailsDto addPackagingVariant(String productId, PackagingDetailsDto dto, Long userId) {

        Seller seller = sellerRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        ProductDetails product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!product.getSeller().getSellerId().equals(seller.getSellerId())) {
            throw new RuntimeException("Unauthorized to modify this product");
        }

        if (product.getPackagingDetails() == null) {
            product.setPackagingDetails(new HashSet<>());
        }

        PackagingDetails candidate = packagingDetailsMapper.toEntity(dto);
        PackagingDetails resolved = packagingDetailsService.resolveOrCreatePackaging(
                product, candidate, seller.getSellerName(), seller.getSellerId());

        product.getPackagingDetails().add(resolved);
        productRepo.save(product);

        return packagingDetailsMapper.toDTO(resolved);
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

        if (product.getProductImages() != null && !product.getProductImages().isEmpty()) {
            for (ProductImage image : product.getProductImages()) {

                String imageUrl = image.getProductImage();

                if (imageUrl != null && !imageUrl.isBlank()) {
                    String key = s3Service.extractKeyFromUrl(imageUrl);
                    s3Service.deleteFile(key);
                }
            }
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

        // ✅ BASIC FIELDS
        existingProduct.setProductName(dto.getProductName());
        existingProduct.setWarningsPrecautions(dto.getWarningsPrecautions());
        existingProduct.setProductDescription(dto.getProductDescription());
        existingProduct.setGstPercentage(dto.getGstPercentage());
        existingProduct.setHsnCode(dto.getHsnCode());
        existingProduct.setModifiedBy(seller.getSellerId());
        existingProduct.setModifiedDate(LocalDateTime.now());

        // ✅ CATEGORY
        if (dto.getCategoryId() != null) {
            Category category = categoryRepo.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            existingProduct.setCategory(category);
        }

        // =========================================================
        // ✅ PACKAGING DETAILS — dedupe against existing variants instead of always inserting
        // =========================================================
        PackagingDetails singleResolvedPackaging = null;

        if (dto.getPackagingDetails() != null) {

            if (existingProduct.getPackagingDetails() == null) {
                existingProduct.setPackagingDetails(new HashSet<>());
            }

            for (PackagingDetailsDto pd : dto.getPackagingDetails()) {

                PackagingDetails candidate = packagingDetailsMapper.toEntity(pd);
                PackagingDetails resolved = packagingDetailsService.resolveOrCreatePackaging(
                        existingProduct, candidate, seller.getSellerName(), seller.getSellerId());

                existingProduct.getPackagingDetails().add(resolved);
                singleResolvedPackaging = resolved;
            }
        }

        // =========================================================
        // ✅ PRICING DETAILS (WITH ADDITIONAL DISCOUNTS) — restock an existing batch
        // (same lot number) instead of always inserting a duplicate row.
        // =========================================================
        if (dto.getPricingDetails() != null) {

            if (existingProduct.getPricingDetails() == null) {
                existingProduct.setPricingDetails(new HashSet<>());
            }

            for (PricingDetailsDto pDto : dto.getPricingDetails()) {

                PricingDetails candidate = pricingDetailsMapper.toEntity(pDto);
                PackagingDetails linkTo = resolvePackagingLink(existingProduct, pDto, singleResolvedPackaging);

                PricingDetails pricing = pricingDetailsService.resolveOrCreateBatch(
                        existingProduct, linkTo, candidate, seller.getSellerName(), seller.getSellerId());

                pricing.setModifiedBy(seller.getSellerId());

                existingProduct.getPricingDetails().add(pricing);

                // Additional discounts
                if (pDto.getAdditionalDiscounts() != null) {

                    Set<AdditionalDiscount> discounts = pDto.getAdditionalDiscounts()
                            .stream()
                            .map(d -> {

                                AdditionalDiscount ad;

                                // ✅ UPDATE existing row
                                if (d.getAdditionalDiscountId() != null &&
                                        !d.getAdditionalDiscountId().isBlank()) {

                                    ad = pricing.getAdditionalDiscounts() != null
                                            ? pricing.getAdditionalDiscounts()
                                            .stream()
                                            .filter(existing ->
                                                    Objects.equals(
                                                            existing.getAdditionalDiscountId(),
                                                            d.getAdditionalDiscountId()
                                                    ))
                                            .findFirst()
                                            .orElse(new AdditionalDiscount())
                                            : new AdditionalDiscount();

                                } else {
                                    // ✅ CREATE new row
                                    ad = new AdditionalDiscount();
                                }

                                ad.setMinimumPurchaseQuantity(
                                        d.getMinimumPurchaseQuantity());
                                ad.setAdditionalDiscountPercentage(
                                        d.getAdditionalDiscountPercentage());
                                ad.setEffectiveStartDate(
                                        d.getEffectiveStartDate());
                                ad.setEffectiveStartTime(
                                        d.getEffectiveStartTime());
                                ad.setEffectiveEndDate(
                                        d.getEffectiveEndDate());
                                ad.setEffectiveEndTime(
                                        d.getEffectiveEndTime());
                                ad.setDisplayOffer(
                                        d.getDisplayOffer());

                                ad.setPricingDetails(pricing);

                                return ad;
                            })
                            .collect(Collectors.toSet());

                    pricing.setAdditionalDiscounts(discounts);
                }

//                if (pDto.getAdditionalDiscounts() != null) {
//                    Set<AdditionalDiscount> discounts = pDto.getAdditionalDiscounts().stream()
//                            .map(d -> {
//                                AdditionalDiscount ad = new AdditionalDiscount();
//                                ad.setMinimumPurchaseQuantity(d.getMinimumPurchaseQuantity());
//                                ad.setAdditionalDiscountPercentage(d.getAdditionalDiscountPercentage());
//                                ad.setEffectiveStartDate(d.getEffectiveStartDate());
//                                ad.setEffectiveStartTime(d.getEffectiveStartTime());
//                                ad.setEffectiveEndDate(d.getEffectiveEndDate());
//                                ad.setEffectiveEndTime(d.getEffectiveEndTime());
//                                ad.setPricingDetails(pricing);
//                                return ad;
//                            })
//                            .collect(Collectors.toSet());
//
//                    pricing.setAdditionalDiscounts(discounts);
//                }

                // 🔥 SPECIAL SCHEMES
                if (pDto.getSpecialSchemes() != null) {

                    Set<SpecialSchemes> schemes = pDto.getSpecialSchemes().stream()
                            .map(s -> {

                                SpecialSchemes scheme;

                                // ✅ UPDATE existing row
                                if (s.getSpecialSchemesId() != null &&
                                        !s.getSpecialSchemesId().isBlank()) {

                                    scheme = pricing.getSpecialSchemes() != null
                                            ? pricing.getSpecialSchemes()
                                            .stream()
                                            .filter(existing ->
                                                    Objects.equals(
                                                            existing.getSpecialSchemesId(),
                                                            s.getSpecialSchemesId()
                                                    ))
                                            .findFirst()
                                            .orElse(new SpecialSchemes())
                                            : new SpecialSchemes();

                                } else {
                                    // ✅ CREATE new row
                                    scheme = new SpecialSchemes();
                                }

                                scheme.setSchemeName(s.getSchemeName());
//                                scheme.setSchemeType(s.getSchemeType());
                                scheme.setBuyQuantity(s.getBuyQuantity());
                                scheme.setFreeQuantity(s.getFreeQuantity());
                                scheme.setEffectiveStartDate(s.getEffectiveStartDate());
                                scheme.setEffectiveStartTime(s.getEffectiveStartTime());
                                scheme.setEffectiveEndDate(s.getEffectiveEndDate());
                                scheme.setEffectiveEndTime(s.getEffectiveEndTime());

                                scheme.setPricingDetails(pricing);

                                return scheme;
                            })
                            .collect(Collectors.toSet());

                    pricing.setSpecialSchemes(schemes);
                }
            }
        }


        // =========================================================
        // ✅ PRODUCT ATTRIBUTE DRUG + MOLECULES
        // =========================================================

        if (dto.getProductAttributeDrugs() != null && !dto.getProductAttributeDrugs().isEmpty()) {

            ProductAttributeDrugDto attrDto =
                    dto.getProductAttributeDrugs().iterator().next();
            Set<ProductAttributeDrug> existingAttrs = existingProduct.getProductAttributeDrugs();

            ProductAttributeDrug attr;

            // ✅ Get existing OR create new
            if (existingAttrs != null && !existingAttrs.isEmpty()) {
                attr = existingAttrs.iterator().next();
            } else {
                attr = new ProductAttributeDrug();
                attr.setProductDetails(existingProduct);
                attr.setCreatedBy(seller.getSellerId());

                if (existingAttrs == null) {
                    existingAttrs = new HashSet<>();
                    existingProduct.setProductAttributeDrugs(existingAttrs);
                }

                existingAttrs.add(attr);
            }

            // ✅ UPDATE FIELDS
            attr.setTherapeuticCategoryId(attrDto.getTherapeuticCategoryId());
            attr.setTherapeuticSubcategoryId(attrDto.getTherapeuticSubcategoryId());
            attr.setDosageForm(attrDto.getDosageForm());
            attr.setModifiedBy(seller.getSellerId());
            attr.setModifiedDate(LocalDateTime.now());

            // =====================================================
            // 🔥 MOLECULES (ONLY ADD NEW ROWS - FIXED)
            // =====================================================
            if (attrDto.getMolecules() != null) {

                // ✅ CLEAR OLD (CRITICAL FIX)
                if (attr.getProductMolecules() != null) {
                    attr.getProductMolecules().clear();
                } else {
                    attr.setProductMolecules(new HashSet<>());
                }

                for (var mDto : attrDto.getMolecules()) {

                    Long moleculeId = mDto.getMoleculeId();

                    Molecule molecule = moleculeRepo.findById(moleculeId)
                            .orElseThrow(() -> new RuntimeException("Molecule not found: " + moleculeId));

                    ProductMolecule pm = new ProductMolecule();

                    ProductMoleculeId id = new ProductMoleculeId();
                    id.setProductAttributeId(attr.getProductAttributeId());
                    id.setMoleculeId(moleculeId);

                    pm.setId(id);
                    pm.setProductAttributeDrug(attr);
                    pm.setMolecule(molecule);
                    pm.setStrength(mDto.getStrength());

                    attr.getProductMolecules().add(pm);
                }
            }

            // =====================================================
            // 🔥 STORAGE CONDITIONS (UPDATE EXISTING ROW)
            // =====================================================
            if (attrDto.getStorageConditionIds() != null) {

                // ✅ Clear old conditions
                if (attr.getStorageConditions() != null) {
                    attr.getStorageConditions().clear();
                } else {
                    attr.setStorageConditions(new HashSet<>());
                }

                // ✅ Add new ones
                for (Long scId : attrDto.getStorageConditionIds()) {

                    StorageConditionMaster sc = storageConditionMasterRepository
                            .findById(scId)
                            .orElseThrow(() -> new RuntimeException("Storage condition not found: " + scId));

                    attr.getStorageConditions().add(sc);
                }
            }

            // =====================================================
            // 🔥 USER MANUAL (REPLACE OLD WITH NEW)
            // =====================================================
            if (attrDto.getUserManualUrl() != null) {

                // ❌ Remove existing manual (if any)
                if (attr.getProductUserManual() != null) {
                    attr.setProductUserManual(null); // orphanRemoval will delete it
                }

                // ✅ Add new manual
                ProductUserManual newManual = new ProductUserManual();
                newManual.setUserManualUrl(attrDto.getUserManualUrl());
                newManual.setProductAttributeDrug(attr);

                attr.setProductUserManual(newManual);
            } else {

                // ❌ If explicitly removed from UI → delete existing
                if (attr.getProductUserManual() != null) {
                    attr.setProductUserManual(null);
                }
            }
        }

        // Non-Consumable Attribute Update
        if (dto.getProductAttributeNonConsumableMedicals() != null && !dto.getProductAttributeNonConsumableMedicals().isEmpty()) {

            ProductAttributeNonConsumableMedicalDto nonConsumableDto =
                    dto.getProductAttributeNonConsumableMedicals().iterator().next();

            Set<ProductAttributeNonConsumableMedical> existingNonConsumables =
                    existingProduct.getProductAttributeNonConsumableMedicals();

            if (existingNonConsumables != null && !existingNonConsumables.isEmpty()) {

                ProductAttributeNonConsumableMedical existing =
                        existingNonConsumables.iterator().next();

                // ✅ ONLY UPDATE THESE 5 FIELDS
                existing.setPurpose(nonConsumableDto.getPurpose());
                existing.setKeyFeaturesSpecifications(nonConsumableDto.getKeyFeaturesSpecifications());
                existing.setWarrantyPeriod(nonConsumableDto.getWarrantyPeriod());
                existing.setServiceAvailability(nonConsumableDto.isServiceAvailability());

                if (nonConsumableDto.getStorageConditionId() != null) {
                    StorageConditionMaster storageCondition = storageConditionMasterRepository
                            .findById(nonConsumableDto.getStorageConditionId())
                            .orElseThrow(() -> new RuntimeException("Storage condition not found"));
                    existing.setStorageConditionMaster(storageCondition);
                }

                existing.setModifiedBy(seller.getSellerId());
                existing.setModifiedDate(LocalDateTime.now());
            }
        }

        //consumable
        if (dto.getProductAttributeConsumableMedicals() != null && !dto.getProductAttributeConsumableMedicals().isEmpty()) {

            ConsumableProductAttributeDTO ConsumableDto =
                    dto.getProductAttributeConsumableMedicals().iterator().next();

            Set<ProductAttributeConsumableMedical> existingConsumables =
                    existingProduct.getProductAttributeConsumables();

            if (existingConsumables != null && !existingConsumables.isEmpty()) {

                ProductAttributeConsumableMedical existing =
                        existingConsumables.iterator().next();

                // ONLY UPDATE THESE 4 FIELDS
                existing.setPurpose(ConsumableDto.getPurpose());
                existing.setKeyFeaturesSpecifications(ConsumableDto.getKeyFeaturesSpecifications());
                existing.setSafetyInstructions(ConsumableDto.getSafetyInstructions());

                if (ConsumableDto.getStorageConditionId() != null) {
                    StorageConditionMaster storageCondition = storageConditionMasterRepository
                            .findById(ConsumableDto.getStorageConditionId())
                            .orElseThrow(() -> new RuntimeException("Storage condition not found"));
                    existing.setStorageConditionMaster(storageCondition);
                }

                existing.setModifiedBy(seller.getSellerId());
                existing.setModifiedDate(LocalDateTime.now());
            }
        }

        // Supplements Or Nutraceuticals Attribute Update
        if (dto.getProductAttributeSupplementsOrNutraceuticals() != null && !dto.getProductAttributeSupplementsOrNutraceuticals().isEmpty()) {
            ProductAttributeSupplementsOrNutraceuticalsDto supplementsDto =
                    dto.getProductAttributeSupplementsOrNutraceuticals().iterator().next();

            Set<ProductAttributeSupplementsOrNutraceuticals> existingSupplements =
                    existingProduct.getProductAttributeSupplementsOrNutraceuticals();

            if (existingSupplements != null && !existingSupplements.isEmpty()) {

                ProductAttributeSupplementsOrNutraceuticals existing =
                        existingSupplements.iterator().next();

                // ✅ ONLY UPDATE THESE FIELDS
                existing.setVariantName(supplementsDto.getVariantName());
                existing.setOtherIngredients(supplementsDto.getOtherIngredients());
                existing.setNutritionalInformation(supplementsDto.getNutritionalInformation());
                existing.setIntendedUse(supplementsDto.getIntendedUse());
                existing.setGender(supplementsDto.getGender());
                existing.setAllergenInformation(supplementsDto.getAllergenInformation());

                if (supplementsDto.getFlavourId() != null &&
                        !Objects.equals(existing.getFlavour().getFlavourId(), supplementsDto.getFlavourId())) {
                    Flavour flavour = flavourRepository
                            .findById(supplementsDto.getFlavourId())
                            .orElseThrow(() -> new NotFoundException("Flavour not found"));
                    existing.setFlavour(flavour);
                }

                existing.setProductClaims(supplementsDto.getProductClaims());

                if (supplementsDto.getStorageConditionId() != null &&
                        !Objects.equals(existing.getStorageConditionMaster().getStorageConditionId(), supplementsDto.getStorageConditionId())) {

                    Set<PricingDetails> pricingDetailsSet = existingProduct.getPricingDetails();

                    boolean hasStock = pricingDetailsSet != null &&
                            pricingDetailsSet.stream()
                                    .max(Comparator.comparing(PricingDetails::getCreatedDate,
                                            Comparator.nullsLast(Comparator.naturalOrder())))
                                    .map(latest -> latest.getStockQuantity() != null && latest.getStockQuantity() > 0)
                                    .orElse(false);

                    if (hasStock) {
                        throw new ApplicationException(
                                "Storage condition cannot be changed while stock is available. " +
                                        "Allowed only when stock quantity is 0."
                        );
                    }
                    StorageConditionMaster storageCondition = storageConditionMasterRepository
                            .findById(supplementsDto.getStorageConditionId())
                            .orElseThrow(() -> new NotFoundException("Storage condition not found"));
                    existing.setStorageConditionMaster(storageCondition);
                }

                // =====================================================
                // 🔥 CERTIFICATE DOCUMENTS (ONLY INSERT NEW — NEVER TOUCH EXISTING)
                // =====================================================
                if (supplementsDto.getCertificateDocuments() != null && !supplementsDto.getCertificateDocuments().isEmpty()) {

                    // Build a Set of existing certificationIds already linked to this attribute
                    Set<Long> existingCertificationIds = existing.getCertificateDocuments()
                            .stream()
                            .filter(doc -> doc.getCertification() != null)
                            .map(doc -> doc.getCertification().getCertificationId())
                            .collect(Collectors.toSet());

                    for (ProductCertificateDocumentDto docDto : supplementsDto.getCertificateDocuments()) {

                        if (docDto.getCertificationId() == null) continue;

                        // ✅ Only insert if this certificationId is NOT already present
                        if (!existingCertificationIds.contains(docDto.getCertificationId())) {

                            Certification certification = certificationRepository
                                    .findById(docDto.getCertificationId())
                                    .orElseThrow(() -> new RuntimeException("Certification not found: " + docDto.getCertificationId()));

                            ProductCertificateDocument newDoc = ProductCertificateDocument.builder()
                                    .certification(certification)
                                    .supplementsOrNutraceuticals(existing)
                                    .isActive(true)
                                    .createdBy(seller.getSellerId())
                                    .build();

                            existing.getCertificateDocuments().add(newDoc);
                        }
                        // ✅ If already exists — skip entirely, your separate upload endpoint handles it
                    }
                }

                existing.setModifiedBy(seller.getSellerId());
                existing.setModifiedDate(LocalDateTime.now());
            }
        }

        // Cosmetic and personal use Update
        if (dto.getProductAttributeCosmeticAndPersonalUse() != null && !dto.getProductAttributeCosmeticAndPersonalUse().isEmpty()) {
            CosmeticAndPersonalUseProductAttributeDTO cosmeticDto =
                    dto.getProductAttributeCosmeticAndPersonalUse().iterator().next();

            Set<ProductAttributeCosmeticandPersonalCare> existingCosmetic =
                    existingProduct.getProductAttributeCosmeticandPersonalCare();

            if (existingCosmetic != null && !existingCosmetic.isEmpty()) {

                ProductAttributeCosmeticandPersonalCare existing =
                        existingCosmetic.iterator().next();

                //  ONLY UPDATE THESE 5 FIELDS
                existing.setProductClaims(cosmeticDto.getProductClaims());
                existing.setVariantName(cosmeticDto.getVariantName());


                if (cosmeticDto.getUseAreaId() != null && !cosmeticDto.getUseAreaId().isEmpty()) {
                    List<Long> useAreaIds = cosmeticDto.getUseAreaId();

                    List<IntendedUseArea> intendedUseAreas = intendedUseAreaRepository
                            .findAllById(useAreaIds);

                    if (intendedUseAreas.size() != useAreaIds.size()) {
                        throw new RuntimeException("Some Intended Areas not found");
                    }

                    existing.setIntendedUseArea(intendedUseAreas);  // List -> List
                }

                if (cosmeticDto.getStorageConditionId() != null &&
                            !Objects.equals(existing.getStorageConditionMaster().getStorageConditionId(), cosmeticDto.getStorageConditionId())) {

                        Set<PricingDetails> pricingDetailsSet = existingProduct.getPricingDetails();

                        boolean hasStock = pricingDetailsSet != null &&
                                pricingDetailsSet.stream()
                                        .max(Comparator.comparing(PricingDetails::getCreatedDate,
                                                Comparator.nullsLast(Comparator.naturalOrder())))
                                        .map(latest -> latest.getStockQuantity() != null && latest.getStockQuantity() > 0)
                                        .orElse(false);

                        if (hasStock) {
                            throw new ApplicationException(
                                    "Storage condition cannot be changed while stock is available. " +
                                            "Allowed only when stock quantity is 0."
                            );
                        }
                        StorageConditionMaster storageCondition = storageConditionMasterRepository
                                .findById(cosmeticDto.getStorageConditionId())
                                .orElseThrow(() -> new RuntimeException("Storage condition not found"));
                        existing.setStorageConditionMaster(storageCondition);
                }

                // =====================================================
                //  CERTIFICATE DOCUMENTS (ONLY INSERT NEW — NEVER TOUCH EXISTING)
                // =====================================================
                if (cosmeticDto.getCertificateDocuments() != null && !cosmeticDto.getCertificateDocuments().isEmpty()) {

                    // Build a Set of existing certificationIds already linked to this attribute
                    Set<Long> existingCertificationIds = existing.getCertificateDocuments()
                            .stream()
                            .filter(doc -> doc.getCertification() != null)
                            .map(doc -> doc.getCertification().getCertificationId())
                            .collect(Collectors.toSet());

                    for (ProductCertificateDocumentDto docDto : cosmeticDto.getCertificateDocuments()) {

                        if (docDto.getCertificationId() == null) continue;

                        //  Only insert if this certificationId is NOT already present
                        if (!existingCertificationIds.contains(docDto.getCertificationId())) {

                            Certification certification = certificationRepository
                                    .findById(docDto.getCertificationId())
                                    .orElseThrow(() -> new RuntimeException("Certification not found: " + docDto.getCertificationId()));

                            ProductCertificateDocument newDoc = ProductCertificateDocument.builder()
                                    .certification(certification)
                                    .cosmeticAndPersonalUse(existing)
                                    .isActive(true)
                                    .createdBy(seller.getSellerId())
                                    .build();

                            existing.getCertificateDocuments().add(newDoc);
                        }
                        // If already exists — skip entirely, your separate upload endpoint handles it
                    }
                }
                existing.setModifiedBy(seller.getSellerId());
                existing.setModifiedDate(LocalDateTime.now());
            }
        }


// ✅ FOOD & INFANT ATTRIBUTE UPDATE
        if (dto.getProductAttributeFoodInfants() != null
                && !dto.getProductAttributeFoodInfants().isEmpty()) {

            ProductAttributeFoodInfantDto foodDto =
                    dto.getProductAttributeFoodInfants().iterator().next();

            Set<ProductAttributeFoodInfant> existingFoodAttrs =
                    existingProduct.getProductAttributeFoodInfants();

            ProductAttributeFoodInfant foodAttr;


            if (existingFoodAttrs != null && !existingFoodAttrs.isEmpty()) {

                foodAttr = existingFoodAttrs.iterator().next();

                foodDto.setProductAttributeId(
                        foodAttr.getProductAttributeId());

                foodDto.setCreatedBy(foodAttr.getCreatedBy());
                foodDto.setCreatedDate(foodAttr.getCreatedDate());

            } else {

                foodDto.setCreatedBy(seller.getSellerId());
                foodDto.setCreatedDate(LocalDateTime.now());
            }

            ProductAttributeFoodInfant mapped =
                    productAttributeFoodInfantMapper.toEntity(foodDto);

            mapped.setProductDetails(existingProduct);

            mapped.setModifiedBy(seller.getSellerId());
            mapped.setModifiedDate(LocalDateTime.now());


            if (existingFoodAttrs == null) {
                existingFoodAttrs = new HashSet<>();
                existingProduct.setProductAttributeFoodInfants(existingFoodAttrs);
            } else {
                existingFoodAttrs.clear();
            }

            existingFoodAttrs.add(mapped);
        }

       //  PRODUCT IMAGES UPDATE
        productImageService.updateProductImages(
                existingProduct,
                dto.getRetainedImageUrls()
        );

        // =========================================================
        // ✅ SAVE
        // =========================================================
        ProductDetails updatedProduct = productRepo.save(existingProduct);

        return productMapper.toDto(updatedProduct);
    }


    @Override
    public List<TherapeuticSubcategoryDto> getSubcategories(String categoryId) {
        return productRepo.findByCategoryId(categoryId);

    }

}