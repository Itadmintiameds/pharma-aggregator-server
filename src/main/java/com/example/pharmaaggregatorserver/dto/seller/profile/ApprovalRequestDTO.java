package com.example.pharmaaggregatorserver.dto.seller.profile;

import lombok.Data;

@Data
public class ApprovalRequestDTO {
    private Long pendingSellerId;
    private String action; // "APPROVE" or "REJECT"
    private String rejectionReason; // Required if action is REJECT
    private String approvedBy; // Add this field
}