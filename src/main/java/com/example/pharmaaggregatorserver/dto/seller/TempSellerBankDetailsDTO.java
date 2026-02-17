package com.example.pharmaaggregatorserver.dto.seller;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class TempSellerBankDetailsDTO {
    @NotBlank(message = "Bank name is required")
    @Size(max = 100, message = "Bank name cannot exceed 100 characters")
    private String bankName;

    @NotBlank(message = "Branch is required")
    @Size(max = 100, message = "Branch cannot exceed 100 characters")
    private String branch;

    @NotBlank(message = "IFSC code is required")
    @Size(max = 100, message = "IFSC code cannot exceed 100 characters")
    private String ifscCode;

    @NotBlank(message = "Account number is required")
    @Size(max = 100, message = "Account number cannot exceed 100 characters")
    private String accountNumber;

    @NotBlank(message = "Account holder name is required")
    @Size(max = 100, message = "Account holder name cannot exceed 100 characters")
    private String accountHolderName;

    @NotBlank(message = "Bank Document File URL is required")
    private String bankDocumentFileUrl;
}