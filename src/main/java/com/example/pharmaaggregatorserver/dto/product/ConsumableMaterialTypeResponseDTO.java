package com.example.pharmaaggregatorserver.dto.product;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsumableMaterialTypeResponseDTO {
    private Long materialTypeId;
    private String materialTypeName;
    private String description;
}