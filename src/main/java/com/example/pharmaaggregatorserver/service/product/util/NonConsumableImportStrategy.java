package com.example.pharmaaggregatorserver.service.product.util;

import com.example.pharmaaggregatorserver.dto.product.ProductAttributeNonConsumableMedicalDto;
import com.example.pharmaaggregatorserver.dto.product.ProductDetailsDto;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Component;

import java.util.Set;

import static com.example.pharmaaggregatorserver.service.product.util.ExcelImportUtils.getString;

@Component("NON_CONSUMABLE")
public class NonConsumableImportStrategy implements ProductImportStrategy {

    @Override
    public ProductDetailsDto mapRow(Row row) {

        ProductDetailsDto dto = new ProductDetailsDto();

        ProductAttributeNonConsumableMedicalDto attr =
                new ProductAttributeNonConsumableMedicalDto();

        attr.setDeviceName(getString(row, 3));

        dto.setProductAttributeNonConsumableMedicals(Set.of(attr));

        return dto;
    }
}
