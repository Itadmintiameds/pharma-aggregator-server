package com.example.pharmaaggregatorserver.dto.seller;

import com.example.pharmaaggregatorserver.dto.master.ResponseDTO.ProductTypeResponseDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@SuperBuilder
public class SellerDTO {

    private String sellerId;

    private String sellerName;

    private SellerAddressDTO address;

    private SellerCoordinatorDTO coordinator;

    private SellerBankDetailsDTO bankDetails;

    private SellerGSTDTO sellerGST;

    private List<SellerDocumentDto> documents = new ArrayList<>();

    private List<Long> productTypeIds;

    private List<ProductTypeResponseDTO> productTypes = new ArrayList<>();

    private Long companyTypeId;
    private String companyTypeName;

    private Long sellerTypeId;
    private String sellerTypeName;

    private String phone;

    private boolean isPhoneVerified;

    private String email;

    private boolean isEmailVerified;

    private String website;

    private String status; // Approved, Correction_required, Rejected

    private boolean termsAccepted;

    private boolean isPasswordTemporary;
}
