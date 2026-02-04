package com.example.pharmaaggregatorserver.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonPropertyOrder({
        "id",
        "stateCode",
        "stateName",
        "isActive"
})
public class StateDto {

    private Long id;
    private String stateCode;
    private String stateName;
    private Boolean isActive;
}
