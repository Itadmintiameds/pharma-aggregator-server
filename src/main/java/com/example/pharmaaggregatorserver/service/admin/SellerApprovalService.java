package com.example.pharmaaggregatorserver.service.admin;

import com.example.pharmaaggregatorserver.dto.seller.SellerApprovalRequestDTO;
import com.example.pharmaaggregatorserver.dto.seller.SellerApprovalResultDTO;

public interface SellerApprovalService {

    SellerApprovalResultDTO processReview(SellerApprovalRequestDTO request);
}
