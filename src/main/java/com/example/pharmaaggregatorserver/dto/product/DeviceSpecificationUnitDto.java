package com.example.pharmaaggregatorserver.dto.product;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonPropertyOrder({"unitId", "unitName", "deviceSubCategoryId", "deviceSubCategoryName"})
public class DeviceSpecificationUnitDto {

    private Long unitId;
    private String unitName;
    private Long deviceSubCategoryId;
    private String deviceSubCategoryName;
}
