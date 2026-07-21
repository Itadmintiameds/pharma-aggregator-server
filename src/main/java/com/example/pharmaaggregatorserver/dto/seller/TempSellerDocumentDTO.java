package com.example.pharmaaggregatorserver.dto.seller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TempSellerDocumentDTO {

    // Set for product-tied licences (e.g. Drug Manufacturing/Wholesale Licence);
    // documentTypeId is not required in this case, since productTypeId already
    // identifies the row. Left null for seller-level documents (agreements,
    // certificates), which have no product type and require documentTypeId
    // instead — exactly one of the two must be present (enforced in the service
    // layer, since Bean Validation can't express "either/or" across two fields).
    private Long productTypeId;

    private Long documentTypeId;

    @NotBlank(message = "Document number is required")
    @Size(max = 100, message = "Document number cannot exceed 100 characters")
    private String documentNumber;

    @NotBlank(message = "Document file URL is required")
    private String documentFileUrl;

    private boolean isDocumentVerified;

    // New license fields
    private LocalDate licenseIssueDate;

    private LocalDate licenseExpiryDate;

    @Size(max = 255, message = "License issuing authority cannot exceed 255 characters")
    private String licenseIssuingAuthority;

}
