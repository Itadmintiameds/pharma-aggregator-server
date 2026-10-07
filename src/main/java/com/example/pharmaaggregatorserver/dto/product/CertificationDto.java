package com.example.pharmaaggregatorserver.dto.product;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CertificationDto {

    private Long certificationId;
    private String certificationName;
    private Long categoryId;
    private String categoryName;
}
