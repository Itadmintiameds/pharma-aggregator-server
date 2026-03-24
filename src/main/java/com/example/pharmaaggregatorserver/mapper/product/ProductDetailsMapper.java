package com.example.pharmaaggregatorserver.mapper.product;

import com.example.pharmaaggregatorserver.dto.product.MoleculeDto;
import com.example.pharmaaggregatorserver.dto.product.PackagingDetailsDto;
import com.example.pharmaaggregatorserver.dto.product.ProductDetailsDto;
import com.example.pharmaaggregatorserver.entity.product.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductDetailsMapper {

    private final PackagingDetailsMapper packagingMapper;
    private final PricingDetailsMapper pricingMapper;
    private final ProductAttributeDrugMapper attributeMapper;
    private final ProductImageMapper imageMapper;
    private final MoleculeMapper moleculeMapper;


    public ProductDetails toEntity(ProductDetailsDto dto){
        if (dto == null) return null;

        ProductDetails entity = new ProductDetails();
        entity.setProductId(dto.getProductId());
        entity.setProductName(dto.getProductName());
        entity.setWarningsPrecautions(dto.getWarningsPrecautions());
        entity.setProductDescription(dto.getProductDescription());
        entity.setProductMarketingUrl(dto.getProductMarketingUrl());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setModifiedBy(dto.getModifiedBy());
        entity.setCreatedDate(dto.getCreatedDate());
        entity.setModifiedDate(dto.getModifiedDate());

        if (dto.getMolecules() != null) {
            entity.setMolecules(
                    dto.getMolecules().stream()
                            .map(m -> moleculeMapper.fromId(m.getMoleculeId()))
                            .collect(Collectors.toSet())
            );
        }

        if (dto.getPackagingDetails() != null) {
            PackagingDetails packaging = dto.getPackagingDetails();
            packaging.setProductDetails(entity); // IMPORTANT
            entity.setPackagingDetails(packaging);
        }


        if (dto.getPricingDetails() != null) {
            Set<PricingDetails> pricingSet = dto.getPricingDetails().stream()
                    .peek((PricingDetails p) -> p.setProductDetails(entity)) // IMPORTANT
                    .collect(Collectors.toSet());

            entity.setPricingDetails(pricingSet);
        }


        if (dto.getProductAttributeDrugs() != null) {
            Set<ProductAttributeDrug> attrSet = dto.getProductAttributeDrugs().stream()
                    .peek((ProductAttributeDrug a) -> a.setProductDetails(entity)) // IMPORTANT
                    .collect(Collectors.toSet());

            entity.setProductAttributeDrugs(attrSet);
        }


        if (dto.getProductImages() != null) {
            Set<ProductImage> imageSet = dto.getProductImages().stream()
                    .peek((ProductImage i) -> i.setProductDetails(entity)) // IMPORTANT
                    .collect(Collectors.toSet());

            entity.setProductImages(imageSet);
        }


        return entity;

    }


    public ProductDetailsDto toDto(ProductDetails entity) {
        if (entity == null) return null;

        ProductDetailsDto dto = new ProductDetailsDto();

        // 🔹 Basic fields
        dto.setProductId(entity.getProductId());
        dto.setProductName(entity.getProductName());
        dto.setWarningsPrecautions(entity.getWarningsPrecautions());
        dto.setProductDescription(entity.getProductDescription());
        dto.setProductMarketingUrl(entity.getProductMarketingUrl());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setModifiedBy(entity.getModifiedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setModifiedDate(entity.getModifiedDate());

        if (entity.getMolecules() != null) {
            dto.setMolecules(
                    entity.getMolecules().stream()
                            .map(m -> {
                                MoleculeDto mDto = new MoleculeDto();
                                mDto.setMoleculeId(m.getMoleculeId());
                                return mDto;
                            })
                            .collect(Collectors.toSet())
            );
        }

        dto.setPackagingDetails(entity.getPackagingDetails());
        dto.setPricingDetails(entity.getPricingDetails());
        dto.setProductAttributeDrugs(entity.getProductAttributeDrugs());
        dto.setProductImages(entity.getProductImages());

        return dto;
    }
}
