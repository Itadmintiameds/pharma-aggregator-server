package com.example.pharmaaggregatorserver.mapper.product;

import com.example.pharmaaggregatorserver.dto.product.DeviceSpecificationUnitDto;
import com.example.pharmaaggregatorserver.entity.product.MedicalDeviceProductMaster.DeviceSpecificationUnit;
import org.springframework.stereotype.Component;

@Component
public class DeviceSpecificationUnitMapper {

    public DeviceSpecificationUnitDto toDto(DeviceSpecificationUnit entity) {
        DeviceSpecificationUnitDto dto = new DeviceSpecificationUnitDto();
        dto.setUnitId(entity.getUnitId());
        dto.setUnitName(entity.getUnitName());

        if (entity.getDeviceSubCategory() != null) {
            dto.setDeviceSubCategoryId(entity.getDeviceSubCategory().getDeviceSubCatId());
            dto.setDeviceSubCategoryName(entity.getDeviceSubCategory().getSubCategoryName());
        }
        return dto;
    }
}
