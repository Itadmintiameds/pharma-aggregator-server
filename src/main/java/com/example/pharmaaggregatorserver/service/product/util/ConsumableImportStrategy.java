package com.example.pharmaaggregatorserver.service.product.util;

import com.example.pharmaaggregatorserver.dto.product.ConsumableProductAttributeDTO;
import com.example.pharmaaggregatorserver.dto.product.ProductDetailsDto;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Component;

import java.util.Set;

import static com.example.pharmaaggregatorserver.service.product.util.ExcelImportUtils.getString;

@Component("CONSUMABLE")
public class ConsumableImportStrategy implements ProductImportStrategy {

    @Override
    public ProductDetailsDto mapRow(Row row) {

        ProductDetailsDto dto = new ProductDetailsDto();

        ConsumableProductAttributeDTO attr =
                new ConsumableProductAttributeDTO();

        dto.setProductAttributeConsumableMedicals(Set.of(attr));

        return dto;
    }
}
