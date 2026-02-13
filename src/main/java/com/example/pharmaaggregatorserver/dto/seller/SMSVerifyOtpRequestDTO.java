package com.example.pharmaaggregatorserver.dto.seller;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SMSVerifyOtpRequestDTO {
    private String phone;
    private String otp;
}
