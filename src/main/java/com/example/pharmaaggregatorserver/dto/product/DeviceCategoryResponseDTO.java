package com.example.pharmaaggregatorserver.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceCategoryResponseDTO {
    private Long deviceCatId;
    private String deviceName;
    private String deviceCategoryType;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}