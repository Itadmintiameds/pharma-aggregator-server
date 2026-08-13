package com.example.pharmaaggregatorserver.dto.buyer;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class TempBuyerResponseDTO {
    private Long tempBuyerId;
    private String organizationName;
    private String tempBuyerRequestId;
    private String status;
    private LocalDateTime createdAt;

    // Returned so the frontend knows which documentId to send when calling
    // POST /{tempBuyerId}/documents/upload
    private List<DocumentInfo> documents;

    @Getter
    @Setter
    public static class DocumentInfo {
        private Long documentId;
        private String documentTypeName;
    }
}
