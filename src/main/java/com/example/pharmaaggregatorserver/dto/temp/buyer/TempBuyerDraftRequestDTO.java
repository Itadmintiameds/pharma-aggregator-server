package com.example.pharmaaggregatorserver.dto.temp.buyer;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Same field set as {@link TempBuyerRequestDTO}, but with zero bean-validation
 * annotations — every field is truly optional. Used by the "save draft" flow
 * (POST/PUT /temp-buyers/draft...). Mirrors dto.seller.TempSellerDraftRequestDTO.
 */
@Getter
@Setter
public class TempBuyerDraftRequestDTO {

    private String organizationName;

    private Long buyerTypeId;

    private boolean termsAccepted;

    private Long buyerUserId;

    private String orgLogoUrl;

    private String gstNumber;

    private String panNumber;

    private TempBuyerAddressDTO address;
    private TempBuyerContactDTO contact;
    private List<TempBuyerDocumentDto> documents;
}
