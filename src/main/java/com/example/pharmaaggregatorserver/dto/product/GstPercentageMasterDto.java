package com.example.pharmaaggregatorserver.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GstPercentageMasterDto {

    private Long gstPercentageId;
    private BigDecimal gstPercentageValue;
    private Boolean isActive;

}
