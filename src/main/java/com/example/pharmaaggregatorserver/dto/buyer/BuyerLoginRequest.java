package com.example.pharmaaggregatorserver.dto.buyer;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BuyerLoginRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}
