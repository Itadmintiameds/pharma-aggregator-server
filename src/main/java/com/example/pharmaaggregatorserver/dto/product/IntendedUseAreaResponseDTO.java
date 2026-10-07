package com.example.pharmaaggregatorserver.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntendedUseAreaResponseDTO {
    private Long useAreaId;
    private String areaName;
    private String areaCode;
    private String description;
    private String iconClass;
    private Integer displayOrder;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
