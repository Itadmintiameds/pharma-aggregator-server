package com.example.pharmaaggregatorserver.dto.seller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TempSellerDocumentDTO {

    @NotNull(message = "Product type is required")
    private Long productTypeId;

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
