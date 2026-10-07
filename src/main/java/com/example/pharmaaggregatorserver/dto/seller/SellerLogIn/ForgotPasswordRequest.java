package com.example.pharmaaggregatorserver.dto.seller.SellerLogIn;

// ForgotPasswordRequest.java
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;
}
