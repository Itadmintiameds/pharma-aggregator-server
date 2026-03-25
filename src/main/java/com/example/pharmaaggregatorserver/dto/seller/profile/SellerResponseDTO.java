package com.example.pharmaaggregatorserver.dto.seller.profile;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class SellerResponseDTO {
    private Long pendingSellerId;
    private String message;
    private String sellerId;
    private String sellerName;
    private List<Long> productTypeId;
    private Long companyTypeId;
    private Long sellerTypeId;
    private String phone;
    private String email;
    private Boolean termsAccepted;
    private String website;
    private AddressDTO address;
    private CoordinatorDTO coordinator;
    private BankDetailsDTO bankDetails;
    private String gstNumber;
    private String gstFileUrl;
    private List<DocumentDTO> documents;

    @Data
    public static class AddressDTO {
        private Long stateId;
        private Long districtId;
        private Long talukaId;
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
    public static class DocumentDTO {
        private Long pendingSellerDocumentId;
        private Long productTypeId;
        private String productTypeName;
        private String documentNumber;
        private String documentFileUrl;
        private LocalDate licenseIssueDate;
        private LocalDate licenseExpiryDate;
        private String licenseIssuingAuthority;
    }
}