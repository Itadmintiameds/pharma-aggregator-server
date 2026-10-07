package com.example.pharmaaggregatorserver.dto.seller.SellerLogIn;

// ValidateTokenRequest.java

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ValidateTokenRequest {
    @NotBlank(message = "Token is required")
    private String token;
}
