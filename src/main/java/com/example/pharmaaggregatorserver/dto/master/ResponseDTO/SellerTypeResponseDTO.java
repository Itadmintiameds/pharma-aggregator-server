package com.example.pharmaaggregatorserver.dto.master.ResponseDTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SellerTypeResponseDTO {
    private Long sellerTypeId;
    private String sellerTypeName;
    private Boolean isActive;
}
