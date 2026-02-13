package com.example.pharmaaggregatorserver.dto.master.ResponseDTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompanyTypeResponseDTO {
    private Long companyTypeId;
    private String companyTypeName;
    private Boolean isActive;
}
