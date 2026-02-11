package com.example.pharmaaggregatorserver.dto.seller;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailOtpVerifyRequestDTO {

    private Long coordinatorId;
    private String otp;
}