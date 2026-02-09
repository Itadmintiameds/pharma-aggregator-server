package com.example.pharmaaggregatorserver.dto.seller;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class TempSellerCoordinatorDTO {
    @NotBlank(message = "Coordinator name is required")
    @Size(max = 100, message = "Coordinator name cannot exceed 100 characters")
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

    // Add these if you want to track coordinator verification status
    private boolean emailVerified = false;
    private boolean phoneVerified = false;
}