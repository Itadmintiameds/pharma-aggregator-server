package com.example.pharmaaggregatorserver.mapper.product;

import com.example.pharmaaggregatorserver.dto.product.ProductCategoryMasterDto;
import com.example.pharmaaggregatorserver.dto.product.ProductSubcategoryMasterDto;
import com.example.pharmaaggregatorserver.entity.product.ProductCategoryMaster;
import com.example.pharmaaggregatorserver.entity.product.ProductSubcategoryMaster;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ProductCategoryMapper {

    // ProductCategoryMaster Entity → ProductCategoryMaster DTO
    public ProductCategoryMasterDto toDto(ProductCategoryMaster entity) {

        Set<ProductSubcategoryMasterDto> subcategoryMasterDtos = null;

        if (entity.getProductSubcategoryMasters() != null) {
            subcategoryMasterDtos = entity.getProductSubcategoryMasters()
                    .stream()
                    .map(this::toSubcategoryDto)
                    .collect(Collectors.toSet());
        }

        return new ProductCategoryMasterDto(
                entity.getProductCategoryId(),
                entity.getProductCategory(),
                entity.getCategory().getCategoryId(),
                subcategoryMasterDtos
        );
    }

    //  ProductSubcategoryMaster Entity → ProductSubcategoryMaster DTO
    public ProductSubcategoryMasterDto toSubcategoryDto(ProductSubcategoryMaster entity) {
        return new ProductSubcategoryMasterDto(
                entity.getProductSubcategoryId(),
                entity.getProductSubcategory()
        );
    }

    // ProductCategoryMaster DTO →ProductCategoryMaster Entity
    public ProductCategoryMaster toEntity(ProductCategoryMasterDto dto) {
        ProductCategoryMaster entity = new ProductCategoryMaster();
        entity.setProductCategoryId(dto.getProductCategoryId());
        entity.setProductCategory(dto.getProductCategory());
        return entity;
    }

    // ProductSubcategoryMaster DTO → ProductSubcategoryMaster Entity
    public ProductSubcategoryMaster toSubcategoryEntity(ProductSubcategoryMasterDto dto) {
        ProductSubcategoryMaster entity = new ProductSubcategoryMaster();
        entity.setProductSubcategoryId(dto.getProductSubcategoryId());
        entity.setProductSubcategory(dto.getProductSubcategory());
        return entity;
    }
}
