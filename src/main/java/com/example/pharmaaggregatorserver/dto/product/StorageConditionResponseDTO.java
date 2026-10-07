package com.example.pharmaaggregatorserver.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorageConditionResponseDTO {
    private Long storageConditionId;
    private String conditionName;
    private String description;
    private String temperatureRange;
    private Integer displayOrder;
    private Boolean isActive;
}