package com.example.pharmaaggregatorserver.service.admin;

import com.example.pharmaaggregatorserver.dto.seller.SellerApprovalRequestDTO;

public interface SellerApprovalService {

    void processReview(SellerApprovalRequestDTO request);
}
