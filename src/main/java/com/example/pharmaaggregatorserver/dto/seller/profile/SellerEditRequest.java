package com.example.pharmaaggregatorserver.dto.seller.profile;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

@Data
public class SellerEditRequest {

    // Seller basic info
    @NotBlank(message = "Seller name is required")
    private String sellerName;

    @NotNull(message = "Company type is required")
    private Long companyTypeId;

    @NotNull(message = "Seller type is required")
    private Long sellerTypeId;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
    private String phone;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    private String website;
    private Boolean termsAccepted;

    // Address
    private AddressDTO address;

    // Coordinator
    private CoordinatorDTO coordinator;

    // Bank Details
    private BankDetailsDTO bankDetails;

    // GST fields (directly in root)
    private String gstNumber;
    private String gstFileUrl;

    // Documents
    private List<DocumentDTO> documents;

    // Product Types (as array of IDs)
    private List<Long> productTypeId;

    @Data
    public static class AddressDTO {
        @NotNull(message = "State is required")
        private Long stateId;

        @NotNull(message = "District is required")
        private Long districtId;

        @NotNull(message = "Taluka is required")
        private Long talukaId;

        @NotBlank(message = "City is required")
        private String city;

        @NotBlank(message = "Street is required")
        private String street;

        @NotBlank(message = "Building number is required")
        private String buildingNo;

        private String landmark;

        @NotBlank(message = "Pin code is required")
        @Pattern(regexp = "^[0-9]{6}$", message = "Pin code must be 6 digits")
        private String pinCode;
    }

    @Data
    public static class CoordinatorDTO {
        @NotBlank(message = "Coordinator name is required")
        private String name;

        @NotBlank(message = "Designation is required")
        private String designation;

        @Email(message = "Invalid email format")
        @NotBlank(message = "Coordinator email is required")
        private String email;

        @NotBlank(message = "Coordinator mobile is required")
        @Pattern(regexp = "^[0-9]{10}$", message = "Mobile must be 10 digits")
        private String mobile;
    }

    @Data
    public static class BankDetailsDTO {
        @NotBlank(message = "Bank name is required")
        private String bankName;

        @NotBlank(message = "Branch is required")
        private String branch;

        @NotBlank(message = "IFSC code is required")
        @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$", message = "Invalid IFSC code format")
        private String ifscCode;

        @NotBlank(message = "Account number is required")
        private String accountNumber;

        @NotBlank(message = "Account holder name is required")
        private String accountHolderName;

        private String bankDocumentFileUrl;
    }

    @Data
    public static class DocumentDTO {
        private Long documentId; // null for new documents
        @NotNull(message = "Product type ID is required")
        private Long productTypeId;

        @NotBlank(message = "Document number is required")
        private String documentNumber;

        @NotBlank(message = "Document file URL is required")
        private String documentFileUrl;

        private LocalDate licenseIssueDate;
        private LocalDate licenseExpiryDate;
        private String licenseIssuingAuthority;
        private String licenseStatus;
    }
}