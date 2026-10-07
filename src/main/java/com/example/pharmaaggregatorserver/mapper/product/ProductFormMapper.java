package com.example.pharmaaggregatorserver.mapper.product;

import com.example.pharmaaggregatorserver.dto.product.AgeGroupMasterDto;
import com.example.pharmaaggregatorserver.dto.product.ProductFormMasterDto;
import com.example.pharmaaggregatorserver.entity.product.AgeGroupMaster;
import com.example.pharmaaggregatorserver.entity.product.ProductFormMaster;
import org.springframework.stereotype.Component;

@Component
public class ProductFormMapper {

    public ProductFormMasterDto toDto(ProductFormMaster entity) {
        return new ProductFormMasterDto(
                entity.getProductFormId(),
                entity.getProductForm()
        );
    }

    public ProductFormMaster toEntity(ProductFormMasterDto dto) {
        return new ProductFormMaster(
                dto.getProductFormId(),
                dto.getProductForm()
        );
    }
}
