package com.example.pharmaaggregatorserver.dto.product;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PowerSourceDto {

    private Long powerSourceId;
    private String powerSourceName;
}
