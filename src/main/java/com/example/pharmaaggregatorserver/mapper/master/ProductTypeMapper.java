package com.example.pharmaaggregatorserver.mapper.master;

import com.example.pharmaaggregatorserver.dto.master.ResponseDTO.ProductTypeResponseDTO;
import com.example.pharmaaggregatorserver.entity.master.ProductTypeMaster;

public class ProductTypeMapper {

    public static ProductTypeResponseDTO toDto(ProductTypeMaster productType) {
        ProductTypeResponseDTO productTypeResponseDTO = new ProductTypeResponseDTO();
        productTypeResponseDTO.setProductTypeId(productType.getProductTypeId());
        productTypeResponseDTO.setProductTypeName(productType.getProductTypeName());
        productTypeResponseDTO.setIsActive(productType.getIsActive());
        productTypeResponseDTO.setRegulatoryCategory(productType.getRegulatoryCategory());
        return productTypeResponseDTO;
    }
}
