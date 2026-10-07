package com.example.pharmaaggregatorserver.dto.seller.SellerLogIn;

// ResetPasswordWithTokenRequest.java
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordWithTokenRequest {
    @NotBlank(message = "Token is required")
    private String token;

    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$",
            message = "Password must contain at least one digit, one lowercase, one uppercase, one special character and no whitespace")
    private String newPassword;
}
