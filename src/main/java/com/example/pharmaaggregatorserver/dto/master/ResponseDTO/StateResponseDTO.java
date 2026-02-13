package com.example.pharmaaggregatorserver.dto.master.ResponseDTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StateResponseDTO {
    private Long stateId;
    private String stateCode;
    private String stateName;
    private Boolean isActive;
}