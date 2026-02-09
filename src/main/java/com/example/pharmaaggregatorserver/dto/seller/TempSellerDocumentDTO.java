package com.example.pharmaaggregatorserver.dto.seller;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class TempSellerDocumentDTO {
    @NotBlank(message = "GST number is required")
    @Size(max = 100, message = "GST number cannot exceed 100 characters")
    private String gstNumber;

    @NotBlank(message = "GST file URL is required")
    private String gstFileUrl;

    @NotNull(message = "Product type is required")
    private Long productTypeId;

    @NotBlank(message = "Document number is required")
    @Size(max = 100, message = "Document number cannot exceed 100 characters")
    private String documentNumber;

    @NotBlank(message = "Document file URL is required")
    private String documentFileUrl;
}
