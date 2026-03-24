package com.example.pharmaaggregatorserver.mapper.product;

import com.example.pharmaaggregatorserver.dto.product.PackagingDetailsDto;
import com.example.pharmaaggregatorserver.dto.product.ProductAttributeDrugDto;
import com.example.pharmaaggregatorserver.entity.product.PackagingDetails;
import com.example.pharmaaggregatorserver.entity.product.ProductAttributeDrug;
import org.springframework.stereotype.Component;

@Component
public class ProductAttributeDrugMapper {

    public ProductAttributeDrugDto toEntity(ProductAttributeDrugDto dto){
        if (dto == null) return null;

        ProductAttributeDrugDto entity = new ProductAttributeDrugDto();
        entity.setProductAttributeId(dto.getProductAttributeId());
        entity.setTherapeuticCategoryId(dto.getTherapeuticCategoryId());
        entity.setTherapeuticSubcategoryId(dto.getTherapeuticSubcategoryId());
        entity.setDosageForm(dto.getDosageForm());
        entity.setStrength(dto.getStrength());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setModifiedBy(dto.getModifiedBy());
        entity.setCreatedDate(dto.getCreatedDate());
        entity.setModifiedDate(dto.getModifiedDate());
        return entity;

    }


    public ProductAttributeDrugDto toDTO(ProductAttributeDrug entity) {
        if (entity == null) return null;

        ProductAttributeDrugDto dto = new ProductAttributeDrugDto();
        dto.setProductAttributeId(entity.getProductAttributeId());
        dto.setTherapeuticCategoryId(entity.getTherapeuticCategoryId());
        dto.setTherapeuticSubcategoryId(entity.getTherapeuticSubcategoryId());
        dto.setDosageForm(entity.getDosageForm());
        dto.setStrength(entity.getStrength());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setModifiedBy(entity.getModifiedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setModifiedDate(entity.getModifiedDate());
        return dto;
    }

}
