package com.example.pharmaaggregatorserver.dto.seller;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
public class TempSellerResponseDTO {
    private Long tempSellerId;
    private String sellerName;
    private String sellerRequestId;
    private String phone;
    private String email;
    private String status;
    private LocalDateTime createdAt;

    public void setCreatedAt(LocalDateTime createdAt) {
    }
    // Include other fields as needed

    // Returned so the frontend knows which documentId + name to send
    // when calling POST /{tempSellerId}/documents/upload
    private List<DocumentInfo> documents;

    @Getter
    @Setter
    public static class DocumentInfo {
        private Long documentId;
        private String licenseName;   // e.g. "Drug License", "FSSAI"
    }
}