package com.example.pharmaaggregatorserver.dto.product;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
@Data
@Builder
public class SkinTypeResponseDTO {
    private Long typeId;
    private String typeName;
    private String typeCode;
    private String description;
    private Integer displayOrder;
    private String iconClass;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
