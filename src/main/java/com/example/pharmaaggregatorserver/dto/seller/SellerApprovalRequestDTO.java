package com.example.pharmaaggregatorserver.dto.seller;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SellerApprovalRequestDTO {
    private Long id;
    private String status;     // ACCEPT, REJECT, CORRECTION
    private String comments;
}