package com.example.pharmaaggregatorserver.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonPropertyOrder({
        "id",
        "districtCode",
        "districtName",
        "isActive",
        "state"
})
public class DistrictDto {

    private Long id;
    private String districtCode;
    private String districtName;
    private Boolean isActive;
    private StateDto state;
}
