package com.example.pharmaaggregatorserver.mapper.product;

import com.example.pharmaaggregatorserver.dto.product.MoleculeDto;
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
    private final ProductAttributeNonConsumableMedicalMapper attributeNonConsumableMapper;
    private final ProductAttributeConsumableMedicalMapper attributeConsumableMapper;
    private final ProductImageMapper imageMapper;
    private final MoleculeMapper moleculeMapper;


    public ProductDetails toEntity(ProductDetailsDto dto){
        if (dto == null) return null;

        ProductDetails entity = new ProductDetails();

        entity.setProductId(dto.getProductId());
        entity.setProductName(dto.getProductName());
        entity.setWarningsPrecautions(dto.getWarningsPrecautions());
        entity.setProductDescription(dto.getProductDescription());
        entity.setManufacturerName(dto.getManufacturerName());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setModifiedBy(dto.getModifiedBy());
        entity.setCreatedDate(dto.getCreatedDate());
        entity.setModifiedDate(dto.getModifiedDate());


        if (dto.getPackagingDetails() != null) {
            PackagingDetails packaging =
                    packagingMapper.toEntity(dto.getPackagingDetails());
            packaging.setProductDetails(entity);
            entity.setPackagingDetails(packaging);
        }

        if (dto.getPricingDetails() != null) {
            Set<PricingDetails> pricingSet = dto.getPricingDetails().stream()
                    .map(pricingMapper::toEntity)
                    .peek(p -> p.setProductDetails(entity))
                    .collect(Collectors.toSet());

            entity.setPricingDetails(pricingSet);
        }

        if (dto.getProductAttributeDrugs() != null) {
            Set<ProductAttributeDrug> attrSet = dto.getProductAttributeDrugs().stream()
                    .map(attributeMapper::toEntity)
                    .peek(a -> a.setProductDetails(entity))
                    .collect(Collectors.toSet());

            entity.setProductAttributeDrugs(attrSet);
        }

        if (dto.getProductAttributeNonConsumableMedicals() != null) {
            Set<ProductAttributeNonConsumableMedical> attrSet = dto.getProductAttributeNonConsumableMedicals().stream()
                    .map(attributeNonConsumableMapper::toEntity)
                    .peek(a -> a.setProductDetails(entity))
                    .collect(Collectors.toSet());

            entity.setProductAttributeNonConsumableMedicals(attrSet);
        }

        if (dto.getProductAttributeConsumableMedicals() != null) {
            Set<ProductAttributeConsumableMedical> attrSet = dto.getProductAttributeConsumableMedicals().stream()
                    .map(attributeConsumableMapper::toEntity)
                    .peek(a -> a.setProductDetails(entity))
                    .collect(Collectors.toSet());

            entity.setProductAttributeConsumables(attrSet);
        }

        if (dto.getProductImages() != null) {
            Set<ProductImage> imageSet = dto.getProductImages().stream()
                    .map(imageMapper::toEntity)
                    .peek(i -> i.setProductDetails(entity))
                    .collect(Collectors.toSet());

            entity.setProductImages(imageSet);
        }

        return entity;
    }


    public ProductDetailsDto toDto(ProductDetails entity) {
        if (entity == null) return null;

        ProductDetailsDto dto = new ProductDetailsDto();

        dto.setProductId(entity.getProductId());
        dto.setProductName(entity.getProductName());
        if (entity.getCategory() != null) {
            dto.setCategoryId(entity.getCategory().getCategoryId());
        }
        dto.setWarningsPrecautions(entity.getWarningsPrecautions());
        dto.setProductDescription(entity.getProductDescription());
        dto.setManufacturerName(entity.getManufacturerName());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setModifiedBy(entity.getModifiedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setModifiedDate(entity.getModifiedDate());


        if (entity.getPackagingDetails() != null) {
            dto.setPackagingDetails(
                    packagingMapper.toDTO(entity.getPackagingDetails())
            );
        }

        if (entity.getPricingDetails() != null) {
            dto.setPricingDetails(
                    entity.getPricingDetails().stream()
                            .map(pricingMapper::toDTO)
                            .collect(Collectors.toSet())
            );
        }

        if (entity.getProductAttributeDrugs() != null) {
            dto.setProductAttributeDrugs(
                    entity.getProductAttributeDrugs().stream()
                            .map(attributeMapper::toDTO)
                            .collect(Collectors.toSet())
            );
        }

        if (entity.getProductAttributeNonConsumableMedicals() != null) {
            dto.setProductAttributeNonConsumableMedicals(
                    entity.getProductAttributeNonConsumableMedicals().stream()
                            .map(attributeNonConsumableMapper::toDto)
                            .collect(Collectors.toSet())
            );
        }

        if (entity.getProductAttributeConsumables() != null) {
            dto.setProductAttributeConsumableMedicals(
                    entity.getProductAttributeConsumables().stream()
                            .map(attributeConsumableMapper::toDto)
                            .collect(Collectors.toSet())
            );
        }

        if (entity.getProductImages() != null) {
            dto.setProductImages(
                    entity.getProductImages().stream()
                            .map(imageMapper::toDto)
                            .collect(Collectors.toSet())
            );
        }

        return dto;
    }
}