package com.example.pharmaaggregatorserver.dto.master.RequestDTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BuyerTypeMasterDTO {
    private Long buyerTypeId;
    private String buyerTypeName;
    private String buyerTypeAbbreviation;
    private Long mandatoryDocumentTypeId;
    private Boolean isActive;
}
