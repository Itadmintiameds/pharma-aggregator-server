package com.example.pharmaaggregatorserver.dto.temp.buyer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TempBuyerDocumentDto {

    private Long documentTypeId;

    @NotBlank(message = "Document number is required")
    @Size(max = 100, message = "Document number cannot exceed 100 characters")
    private String documentNumber;

    private String documentFileUrl;

    private boolean isDocumentVerified;

    private LocalDate licenseIssueDate;

    private LocalDate licenseExpiryDate;

    @Size(max = 255, message = "License issuing authority cannot exceed 255 characters")
    private String licenseIssuingAuthority;
}
