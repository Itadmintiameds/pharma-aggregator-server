package com.example.pharmaaggregatorserver.dto.temp.buyer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TempBuyerContactDTO {
    @NotBlank(message = "Contact name is required")
    @Size(max = 100, message = "Contact name cannot exceed 100 characters")
    private String name;

    @NotBlank(message = "Designation is required")
    @Size(max = 100, message = "Designation cannot exceed 100 characters")
    private String designation;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    @NotBlank(message = "Mobile is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Mobile must be 10 digits")
    private String mobile;

    private boolean emailVerified = false;
    private boolean phoneVerified = false;
}
