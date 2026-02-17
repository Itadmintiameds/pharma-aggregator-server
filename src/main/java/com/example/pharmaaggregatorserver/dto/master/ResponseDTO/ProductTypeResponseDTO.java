package com.example.pharmaaggregatorserver.dto.master.ResponseDTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductTypeResponseDTO {
    private Long productTypeId;
    private String productTypeName;
    private String regulatoryCategory;
    private Boolean isActive;
}