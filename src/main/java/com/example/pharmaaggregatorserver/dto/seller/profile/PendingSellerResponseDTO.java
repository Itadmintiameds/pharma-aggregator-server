package com.example.pharmaaggregatorserver.dto.seller.profile;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PendingSellerResponseDTO {
    private Long pendingSellerId;
    private String sellerId;
    private String sellerName;
    private String requestType;
    private String status;
    private String requestedBy;
    private LocalDateTime createdAt;

    // Basic Info
    private String phone;
    private String email;
    private String website;
    private Boolean termsAccepted;

    // Company and Seller Types
    private Long companyTypeId;
    private String companyTypeName;
    private Long sellerTypeId;
    private String sellerTypeName;

    // Address
    private AddressDTO address;

    // Coordinator
    private CoordinatorDTO coordinator;

    // Bank Details
    private BankDetailsDTO bankDetails;

    // GST
    private String gstNumber;
    private String gstFileUrl;

    // Company Registration Certificate
    private String companyRegistrationCertificateUrl;

    // Product Types
    private List<ProductTypeDTO> productTypes;

    // Documents
    private List<DocumentDTO> documents;

    @Data
    public static class AddressDTO {
        private Long stateId;
        private String stateName;
        private Long districtId;
        private String districtName;
        private Long talukaId;
        private String talukaName;
        private String city;
        private String street;
        private String buildingNo;
        private String landmark;
        private String pinCode;
    }

    @Data
    public static class CoordinatorDTO {
        private String name;
        private String designation;
        private String email;
        private String mobile;
    }

    @Data
    public static class BankDetailsDTO {
        private String bankName;
        private String branch;
        private String ifscCode;
        private String accountNumber;
        private String accountHolderName;
        private String bankDocumentFileUrl;
    }

    @Data
    public static class ProductTypeDTO {
        private Long productTypeId;
        private String productTypeName;
    }

    @Data
    public static class DocumentDTO {
        private Long productTypeId;
        private String productTypeName;
        private String documentNumber;
        private String documentFileUrl;
        private LocalDateTime licenseIssueDate;
        private LocalDateTime licenseExpiryDate;
        private String licenseIssuingAuthority;
    }
}