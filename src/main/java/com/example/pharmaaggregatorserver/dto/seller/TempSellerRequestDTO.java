package com.example.pharmaaggregatorserver.dto.seller;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TempSellerRequestDTO {
    @NotBlank(message = "Seller name is required")
    @Size(max = 100, message = "Seller name cannot exceed 100 characters")
    private String sellerName;

//    @NotBlank(message = "Seller request ID is required")
//    @Size(max = 100, message = "Seller request ID cannot exceed 100 characters")
//    private String sellerRequestId;

    @NotNull(message = "Product type is required")
    private List<Long> productTypeId;

    @NotNull(message = "Company type is required")
    private Long companyTypeId;

    @NotNull(message = "Seller type is required")
    private Long sellerTypeId;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
    private String phone;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    @AssertTrue(message = "Terms Acceptance is required")
    private boolean termsAccepted;

    private String website;

    private TempSellerAddressDTO address;
    private TempSellerCoordinatorDTO coordinator;
    private TempSellerBankDetailsDTO bankDetails;
    private List<TempSellerDocumentDTO> documents;
}