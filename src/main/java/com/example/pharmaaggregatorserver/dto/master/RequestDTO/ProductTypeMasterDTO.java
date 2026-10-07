package com.example.pharmaaggregatorserver.dto.master.RequestDTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductTypeMasterDTO {
    private Long productTypeId;
    private String productTypeName;
    private String regulatoryCategory;
    private Boolean isActive;
    private String createdBy;
    private String updatedBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
