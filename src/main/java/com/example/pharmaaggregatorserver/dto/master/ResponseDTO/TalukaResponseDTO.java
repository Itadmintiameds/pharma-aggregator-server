package com.example.pharmaaggregatorserver.dto.master.ResponseDTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TalukaResponseDTO {
    private Long talukaId;
    private Long stateId;
    private String stateName;
    private Long districtId;
    private String districtName;
    private String talukaCode;
    private String talukaName;
    private Boolean isActive;
}