package com.example.pharmaaggregatorserver.dto.master.ResponseDTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentTypeResponseDTO {
    private Long documentTypeId;
    private String documentTypeName;
    private String documentTypeCode;
    private Boolean isActive;
}
