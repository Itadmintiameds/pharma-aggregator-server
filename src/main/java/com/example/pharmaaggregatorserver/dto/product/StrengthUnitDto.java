package com.example.pharmaaggregatorserver.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StrengthUnitDto {
    private Long strengthUnitId;
    private String unitName;
    private Boolean isActive;
}
