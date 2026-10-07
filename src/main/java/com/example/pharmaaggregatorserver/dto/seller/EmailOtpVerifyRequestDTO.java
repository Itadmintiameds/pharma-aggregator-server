package com.example.pharmaaggregatorserver.dto.seller;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailOtpVerifyRequestDTO {
    private String email;
    private String otp;

    public String getEmail() {
        return email;
    }

    public String getOtp() {
        return otp;
    }
}