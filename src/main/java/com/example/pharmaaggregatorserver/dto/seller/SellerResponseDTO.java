package com.example.pharmaaggregatorserver.dto.seller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SellerResponseDTO {
    private String sellerId;
    private String sellerName;
    private SellerAddressDTO address;
    private SellerCoordinatorDTO coordinator;
    private SellerBankDetailsDTO bankDetails;
    private SellerGSTDTO sellerGST;
    private List<SellerDocumentDTO> documents;
    private List<ProductTypeDTO> productTypes;
    private CompanyTypeDTO companyType;
    private SellerTypeDTO sellerType;
    private String phone;
    private boolean isPhoneVerified;
    private String email;
    private boolean isEmailVerified;
    private String website;
    private String status;
    private boolean termsAccepted;
    private UserDTO user;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isActive;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SellerAddressDTO {
        private Long sellerAddressId;
        private String city;
        private String street;
        private String buildingNo;
        private String landmark;
        private String pinCode;
        private String state;
        private String district;
        private String taluka;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SellerCoordinatorDTO {
        private Long sellerCoordinatorId;
        private String name;
        private String designation;
        private String email;
        private boolean isEmailVerified;
        private String mobile;
        private boolean isPhoneVerified;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SellerBankDetailsDTO {
        private Long sellerBankDetailsId;
        private String bankName;
        private String branch;
        private String ifscCode;
        private String accountNumber;
        private String accountHolderName;
        private String bankDocumentFileUrl;
        private boolean isBankDocumentVerified;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SellerGSTDTO {
        private Long sellerGstId;
        private String gstNumber;
        private String gstFileUrl;
        private boolean isGstVerified;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SellerDocumentDTO {
        private Long sellerDocumentsId;
        private String documentNumber;
        private String documentFileUrl;
        private boolean isDocumentVerified;
        private String productType;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductTypeDTO {
        private Long productTypeId;
        private String productTypeName;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CompanyTypeDTO {
        private Long companyTypeId;
        private String companyTypeName;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SellerTypeDTO {
        private Long sellerTypeId;
        private String sellerTypeName;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserDTO {
        private Long userId;
        private String username;
        private String email;
    }
}
