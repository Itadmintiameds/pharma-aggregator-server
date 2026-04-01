package com.example.pharmaaggregatorserver.service.product.productImpl;

import com.example.pharmaaggregatorserver.dto.product.ConsumableProductRequestDTO;
import com.example.pharmaaggregatorserver.entity.product.*;
import com.example.pharmaaggregatorserver.repository.*;
import com.example.pharmaaggregatorserver.repository.product.*;
import com.example.pharmaaggregatorserver.repository.seller.SellerRepository;
import com.example.pharmaaggregatorserver.service.product.ConsumableProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConsumableProductServiceImpl implements ConsumableProductService {

    private final ProductDetailsRepository productRepository;

    // FK Repositories
    private final DeviceCategoryRepository deviceCategoryRepository;
    private final MaterialTypeRepository materialTypeRepository;
    private final DimensionRepository dimensionRepository;
    private final CertificationRepository certificationRepository;
    private final PackTypeRepository packTypeRepository;

    private final CategoryRepository categoryRepository;
    private final SellerRepository sellerRepository;

    @Override
    public String createFullProduct(ConsumableProductRequestDTO dto) {

        // ================= PRODUCT =================
        ProductDetails product = new ProductDetails();
        product.setProductId(UUID.randomUUID().toString());
        product.setProductName(dto.getProductName());
        product.setProductDescription(dto.getProductDescription());
        product.setWarningsPrecautions(dto.getWarningsPrecautions());

        product.setCategory(
                categoryRepository.findById(dto.getCategoryId())
                        .orElseThrow(() -> new RuntimeException("Invalid Category ID: " + dto.getCategoryId()))
        );

        product.setSeller(
                sellerRepository.findById(dto.getSellerId())
                        .orElseThrow(() -> new RuntimeException("Invalid Seller ID: " + dto.getSellerId()))
        );

        // ================= ATTRIBUTES =================
        if (dto.getProductAttributes() != null) {

            ProductAttributeConsumableMedical attr = new ProductAttributeConsumableMedical();
            attr.setProductAttributeId(UUID.randomUUID().toString());

            attr.setBrandName(dto.getProductAttributes().getBrandName());
            attr.setSterileOrNonSterile(dto.getProductAttributes().getSterileOrNonSterile());
            attr.setDisposalOrReusable(dto.getProductAttributes().getDisposalOrReusable());
            attr.setShelfLife(dto.getProductAttributes().getShelfLife());
            attr.setPurpose(dto.getProductAttributes().getPurpose());
            attr.setKeyFeaturesSpecifications(dto.getProductAttributes().getKeyFeaturesSpecifications());
            attr.setCountryOfOrigin(dto.getProductAttributes().getCountryOfOrigin());
            attr.setManufacturerName(dto.getProductAttributes().getManufacturerName());
            attr.setStorageCondition(dto.getProductAttributes().getStorageCondition());
            attr.setBrochureType(dto.getProductAttributes().getBrochureType());
            attr.setBrochurePath(dto.getProductAttributes().getBrochurePath());

            attr.setDeviceCategory(deviceCategoryRepository
                    .findById(dto.getProductAttributes().getDeviceCatId())
                    .orElseThrow(() -> new RuntimeException("Invalid Device Category")));

            attr.setMaterialType(materialTypeRepository
                    .findById(dto.getProductAttributes().getMaterialTypeId())
                    .orElseThrow(() -> new RuntimeException("Invalid Material Type")));

            attr.setDimensionSize(dimensionRepository
                    .findById(dto.getProductAttributes().getDiamensionId())
                    .orElseThrow(() -> new RuntimeException("Invalid Dimension")));

            attr.setCertification(certificationRepository
                    .findById(dto.getProductAttributes().getCertificationId())
                    .orElseThrow(() -> new RuntimeException("Invalid Certification")));

            attr.setProductDetails(product);

            Set<ProductAttributeConsumableMedical> attrSet = new HashSet<>();
            attrSet.add(attr);
            product.setProductAttributeConsumables(attrSet);
        }

        // ================= PACKAGING =================
        if (dto.getPackaging() != null) {

            PackagingDetails packaging = new PackagingDetails();
            packaging.setPackagingId(UUID.randomUUID().toString());

            packaging.setUnitPerPack(dto.getPackaging().getUnitPerPack());
            packaging.setNumberOfPacks(dto.getPackaging().getNumberOfPacks());
            packaging.setPackSize(dto.getPackaging().getPackSize());
            packaging.setMinimumOrderQuantity(dto.getPackaging().getMinimumOrderQuantity());
            packaging.setMaximumOrderQuantity(dto.getPackaging().getMaximumOrderQuantity());

            packaging.setPackType(packTypeRepository
                    .findById(dto.getPackaging().getPackId())
                    .orElseThrow(() -> new RuntimeException("Invalid Pack Type")));

            packaging.setProductDetails(product);
            product.setPackagingDetails(packaging);
        }

        // ================= PRICING =================
        if (dto.getPricing() != null) {

            Set<PricingDetails> pricingSet = new HashSet<>();

            dto.getPricing().forEach(p -> {

                PricingDetails pricing = new PricingDetails();
                pricing.setPricingId(UUID.randomUUID().toString());

                pricing.setBatchLotNumber(p.getBatchLotNumber());
                pricing.setManufacturingDate(p.getManufacturingDate());
                pricing.setExpiryDate(p.getExpiryDate());
                pricing.setStockQuantity(p.getStockQuantity());

                // ✅ NEW FIELDS ADDED
                pricing.setPricePerUnit(p.getPricePerUnit()); // Selling Price
                pricing.setMrp(p.getMrp());
                pricing.setDiscountPercentage(p.getDiscountPercentage());
                pricing.setGstPercentage(p.getGstPercentage());
                pricing.setFinalPrice(p.getFinalPrice());
                pricing.setHsnCode(p.getHsnCode());
                pricing.setCreatedDate(p.getCreatedDate()); // Stock entry date

                pricing.setProductDetails(product);

                // ===== ADDITIONAL DISCOUNT =====
                if (p.getAdditionalDiscounts() != null) {

                    Set<AdditionalDiscount> discountSet = new HashSet<>();

                    p.getAdditionalDiscounts().forEach(d -> {

                        AdditionalDiscount discount = new AdditionalDiscount();

                        discount.setMinimumPurchaseQuantity(d.getMinimumPurchaseQuantity());
                        discount.setAdditionalDiscountPercentage(d.getAdditionalDiscountPercentage());

                        discount.setEffectiveStartDate(d.getEffectiveStartDate());
                        discount.setEffectiveStartTime(d.getEffectiveStartTime());

                        discount.setEffectiveEndDate(d.getEffectiveEndDate());
                        discount.setEffectiveEndTime(d.getEffectiveEndTime());

                        discount.setPricingDetails(pricing);

                        discountSet.add(discount);
                    });

                    pricing.setAdditionalDiscounts(discountSet);
                }

                pricingSet.add(pricing);
            });

            product.setPricingDetails(pricingSet);
        }

        // ================= PRODUCT IMAGES =================
        if (dto.getProductImages() != null) {

            Set<ProductImage> imageSet = new HashSet<>();

            dto.getProductImages().forEach(img -> {
                ProductImage image = new ProductImage();
                image.setProductImage(img.getProductImage());
                image.setProductDetails(product);
                imageSet.add(image);
            });

            product.setProductImages(imageSet);
        }

        // ================= SAVE =================
        productRepository.save(product);

        return product.getProductId();
    }
}