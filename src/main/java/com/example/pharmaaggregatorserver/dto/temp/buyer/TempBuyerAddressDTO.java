package com.example.pharmaaggregatorserver.dto.temp.buyer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TempBuyerAddressDTO {
    @NotNull(message = "State is required")
    private Long stateId;

    @NotNull(message = "District is required")
    private Long districtId;

    @NotNull(message = "Taluka is required")
    private Long talukaId;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "Street is required")
    private String street;

    @NotBlank(message = "Building number is required")
    private String buildingNo;

    private String landmark;

    @NotBlank(message = "PIN code is required")
    @Pattern(regexp = "^[0-9]{6}$", message = "PIN code must be 6 digits")
    private String pinCode;
}
