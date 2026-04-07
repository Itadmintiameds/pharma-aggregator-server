package com.example.pharmaaggregatorserver.dto.seller.profile;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SellerUpdateEmailDTO {
    private String coordinatorName;
    private String coordinatorEmail;
    private String sellerCompanyName;
    private String requestId;
    private String sellerId;
    private String rejectionReason;
    private String adminComments;
    private String supportEmail;
}