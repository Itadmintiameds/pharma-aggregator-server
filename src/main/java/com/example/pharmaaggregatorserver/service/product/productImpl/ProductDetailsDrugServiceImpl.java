package com.example.pharmaaggregatorserver.service.product.productImpl;

import com.example.pharmaaggregatorserver.dto.product.PackagingDetailsDrugDto;
import com.example.pharmaaggregatorserver.dto.product.PricingDetailsDrugDto;
import com.example.pharmaaggregatorserver.dto.product.ProductDetailsDrugDto;
import com.example.pharmaaggregatorserver.entity.product.Molecule;
import com.example.pharmaaggregatorserver.entity.product.PackagingDetailsDrug;
import com.example.pharmaaggregatorserver.entity.product.PricingDetailsDrug;
import com.example.pharmaaggregatorserver.entity.product.ProductDetailsDrug;
import com.example.pharmaaggregatorserver.mapper.product.ProductDetailsDrugMapper;
import com.example.pharmaaggregatorserver.repository.product.MoleculeRepository;
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

    @Override
    public ProductDetailsDrugDto createProduct(ProductDetailsDrugDto dto) {

        ProductDetailsDrug product = ProductDetailsDrugMapper.toEntity(dto);

        if (dto.getMolecules() != null && !dto.getMolecules().isEmpty()) {

            Set<Molecule> molecules = dto.getMolecules().stream()
                    .map(m -> moleculeRepository
                            .findByMoleculeNameIgnoreCase(m.getMoleculeName())
                            .orElseGet(() ->
                                    moleculeRepository.save(
                                            new Molecule(null, m.getMoleculeName())
                                    )))
                    .collect(Collectors.toSet());

            product.setMolecules(molecules);
        }

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
    public ProductDetailsDrugDto getProductById(Long productId) {

        ProductDetailsDrug product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Product not found with id: " + productId));

        return ProductDetailsDrugMapper.toDto(product);
    }

    @Override
    public void deleteProduct(Long productId) {

        ProductDetailsDrug product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Product not found with id: " + productId));
        productRepository.delete(product);
    }


    @Override
    public ProductDetailsDrug updateProduct(
            Long productId,
            ProductDetailsDrugDto dto
    ) {

        // 1️⃣ Load existing entity from DB
        ProductDetailsDrug existingProduct =
                productRepository.findById(productId)
                        .orElseThrow(() -> new RuntimeException("Product not found"));

        /* =========================
           UPDATE PRODUCT FIELDS
           ========================= */
        existingProduct.setProductName(dto.getProductName());
        existingProduct.setTherapeuticCategory(dto.getTherapeuticCategory());
        existingProduct.setTherapeuticSubcategory(dto.getTherapeuticSubcategory());
        existingProduct.setDosageForm(dto.getDosageForm());
        existingProduct.setStrength(dto.getStrength());
        existingProduct.setWarningsPrecautions(dto.getWarningsPrecautions());
        existingProduct.setProductDescription(dto.getProductDescription());
        existingProduct.setProductImage(dto.getProductImage());
        existingProduct.setProductMarketingUrl(dto.getProductMarketingUrl());

        /* =========================
           UPDATE PACKAGING
           ========================= */
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

        /* =========================
           INSERT NEW PRICING ROWS
           ========================= */
        if (dto.getPricingDetails() != null) {
            for (PricingDetailsDrugDto priceDto : dto.getPricingDetails()) {

                // Ignore old pricing
                if (priceDto.getPricingId() != null) continue;

                PricingDetailsDrug pricing =
                        ProductDetailsDrugMapper.toPricingEntity(priceDto);

                pricing.setPricingId(null); // force insert
                pricing.setProduct(existingProduct);

                pricingRepository.save(pricing);
            }
        }

        return productRepository.save(existingProduct);
    }

}
