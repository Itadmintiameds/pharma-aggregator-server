package com.example.pharmaaggregatorserver.service.admin;

import com.example.pharmaaggregatorserver.dto.buyer.BuyerApprovalRequestDTO;
import com.example.pharmaaggregatorserver.dto.buyer.BuyerApprovalResultDTO;

public interface BuyerApprovalService {
    BuyerApprovalResultDTO processReview(BuyerApprovalRequestDTO request);
}
