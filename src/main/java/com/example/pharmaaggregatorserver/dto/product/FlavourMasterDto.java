package com.example.pharmaaggregatorserver.dto.product;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FlavourMasterDto {

    private Long flavourId;
    private String flavourName;
}
