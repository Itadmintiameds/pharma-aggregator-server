package com.example.pharmaaggregatorserver.dto.temp.buyer;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TempBuyerRequestDTO {

    @NotBlank(message = "Organization name is required")
    @Size(max = 150, message = "Organization name cannot exceed 150 characters")
    private String organizationName;

    @NotNull(message = "Buyer type is required")
    private Long buyerTypeId;

    @AssertTrue(message = "Terms Acceptance is required")
    private boolean termsAccepted;

    // Linked BuyerUser account (signup-first flow), if any — BuyerUser has no
    // Spring Security principal wired up yet, so it is passed explicitly
    // rather than resolved from the SecurityContext (see TempSellerServiceImpl
    // for the pattern this would otherwise mirror).
    private Long buyerUserId;

    private String orgLogoUrl;

    // Exactly one of gstNumber / panNumber must be present — enforced in
    // TempBuyerServiceImpl (PAN required when GST is blank) since Bean
    // Validation can't express an either/or across two top-level fields.
    private String gstNumber;

    private String panNumber;

    private TempBuyerAddressDTO address;
    private TempBuyerContactDTO contact;
    private List<TempBuyerDocumentDto> documents;
}
