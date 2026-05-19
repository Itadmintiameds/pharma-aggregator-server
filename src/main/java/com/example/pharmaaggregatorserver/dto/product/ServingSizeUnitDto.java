package com.example.pharmaaggregatorserver.dto.product;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonPropertyOrder({
        "id",
        "servingSizeUnit",
        "dosageFormId",
        "dosageFormName",
        "productFormId",
        "productFormName"
})
public class ServingSizeUnitDto {

    private Long id;
    private String servingSizeUnit;
    private Long dosageFormId;
    private String dosageFormName;
    private Long productFormId;
    private String productFormName;
}
