package com.example.pharmaaggregatorserver.mapper.product;

import com.example.pharmaaggregatorserver.dto.product.ServingSizeUnitDto;
import com.example.pharmaaggregatorserver.entity.product.ServingSizeUnit;
import org.springframework.stereotype.Component;

@Component
public class ServingSizeUnitMapper {

    public ServingSizeUnitDto toDto(ServingSizeUnit servingSizeUnit) {
        ServingSizeUnitDto servingSizeUnitDto = new ServingSizeUnitDto();
        servingSizeUnitDto.setId(servingSizeUnit.getId());
        servingSizeUnitDto.setServingSizeUnit(servingSizeUnit.getServingSizeUnit());

        if (servingSizeUnit.getDosageForm() != null) {
            servingSizeUnitDto.setDosageFormId(servingSizeUnit.getDosageForm().getDosageId());
            servingSizeUnitDto.setDosageFormName(servingSizeUnit.getDosageForm().getDosageName());
        }

        if (servingSizeUnit.getProductForm() != null) {
            servingSizeUnitDto.setProductFormId(servingSizeUnit.getProductForm().getProductFormId());
            servingSizeUnitDto.setProductFormName(servingSizeUnit.getProductForm().getProductForm());
        }

        return servingSizeUnitDto;
    }
}
