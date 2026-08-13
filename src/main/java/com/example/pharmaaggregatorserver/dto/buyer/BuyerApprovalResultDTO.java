package com.example.pharmaaggregatorserver.dto.buyer;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BuyerApprovalResultDTO {

    private Long tempBuyerId;
    private Long buyerUserId;
    private String buyerId; // null unless the review action was ACCEPT
    private String status;
}
