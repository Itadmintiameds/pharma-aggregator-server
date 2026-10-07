package com.example.pharmaaggregatorserver.dto.seller;

import jakarta.persistence.Column;
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

    @NotBlank(message = "Company Registration Certificate file URL is required")
    private String companyRegistrationCertificateUrl;

    private String website;

    private String parentManufacturerName;

    private String brandOwnerName;

    @NotBlank(message = "GST number is required")
    @Size(max = 100, message = "GST number cannot exceed 100 characters")
    private String gstNumber;

    @NotBlank(message = "GST file URL is required")
    private String gstFileUrl;

    private boolean isGstVerified = false;

    private TempSellerAddressDTO address;
    private TempSellerCoordinatorDTO coordinator;
    private TempSellerBankDetailsDTO bankDetails;
    private List<TempSellerDocumentDTO> documents;

}