package com.example.pharmaaggregatorserver.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackTypeUnitMasterDto {

    private Long packTypeUnitId;
    private String packTypeUnitName;
    private Boolean isActive;

}

