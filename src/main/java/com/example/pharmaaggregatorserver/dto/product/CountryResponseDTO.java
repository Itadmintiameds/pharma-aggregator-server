package com.example.pharmaaggregatorserver.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CountryResponseDTO {
    private Long countryId;
    private String countryName;
    private String countryCode;
    private String phoneCode;
    private Boolean isActive;
}