package com.example.pharmaaggregatorserver.dto.buyer;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BuyerLogoutRequest {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
