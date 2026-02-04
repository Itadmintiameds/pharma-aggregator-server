package com.example.pharmaaggregatorserver.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonPropertyOrder({
        "id",
        "talukaCode",
        "talukaName",
        "isActive",
        "district"
})
public class TalukaDto {

    private Long id;
    private String talukaCode;
    private String talukaName;
    private Boolean isActive;
    private DistrictDto district;
}
