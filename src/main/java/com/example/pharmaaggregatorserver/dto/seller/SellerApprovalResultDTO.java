package com.example.pharmaaggregatorserver.dto.seller;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SellerApprovalResultDTO {

    private Long tempSellerId;
    private Long userId;
    private String sellerId; // null unless the review action was ACCEPT
    private String status;
}
