package com.example.pharmaaggregatorserver.dto.seller;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Same field set as {@link TempSellerRequestDTO}, but with zero bean-validation
 * annotations — every field is truly optional. Used by the "save draft" flow
 * (POST/PUT /temp-sellers/draft...), which persists partial seller registration
 * data before it is complete. Nested DTOs are reused as-is since they were
 * never {@code @Valid}-cascaded from the parent DTO in the first place.
 */
@Getter
@Setter
public class TempSellerDraftRequestDTO {

    private String sellerName;

    private List<Long> productTypeId;

    private Long companyTypeId;

    private Long sellerTypeId;

    private String phone;

    private String email;

    private boolean termsAccepted;

    private String companyRegistrationCertificateUrl;

    private String website;

    private String parentManufacturerName;

    private String brandOwnerName;

    private String gstNumber;

    private String gstFileUrl;

    private boolean isGstVerified = false;

    private TempSellerAddressDTO address;
    private TempSellerCoordinatorDTO coordinator;
    private TempSellerBankDetailsDTO bankDetails;
    private List<TempSellerDocumentDTO> documents;
}
