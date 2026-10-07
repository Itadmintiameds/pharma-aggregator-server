package com.example.pharmaaggregatorserver.mapper.product;

import com.example.pharmaaggregatorserver.dto.product.GstPercentageMasterDto;
import com.example.pharmaaggregatorserver.entity.product.GstPercentageMaster;
import org.springframework.stereotype.Component;

@Component
public class GstPercentageMasterMapper {

    public GstPercentageMasterDto toDto(GstPercentageMaster entity) {
        return new GstPercentageMasterDto(
                entity.getGstPercentageId(),
                entity.getGstPercentageValue(),
                entity.getIsActive()
        );
    }

}
