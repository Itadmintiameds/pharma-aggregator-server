package com.example.pharmaaggregatorserver.dto.temp.seller;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentVerificationRequestDTO {
    private Long documentId;       // required: target a specific document
    private boolean isDocumentVerified;
}
