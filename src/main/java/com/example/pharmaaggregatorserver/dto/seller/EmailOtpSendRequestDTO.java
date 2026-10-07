package com.example.pharmaaggregatorserver.dto.seller;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailOtpSendRequestDTO {
    private String email;

    public String getEmail() {
        return email;
    }
}