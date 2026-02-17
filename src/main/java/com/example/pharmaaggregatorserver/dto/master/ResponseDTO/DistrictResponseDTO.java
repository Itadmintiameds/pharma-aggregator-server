package com.example.pharmaaggregatorserver.dto.master.ResponseDTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DistrictResponseDTO {
    private Long districtId;
    private Long stateId;
    private String stateName;
    private String districtCode;
    private String districtName;
    private Boolean isActive;
}