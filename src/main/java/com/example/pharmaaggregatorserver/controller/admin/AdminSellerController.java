package com.example.pharmaaggregatorserver.controller.admin;

import com.example.pharmaaggregatorserver.dto.seller.SellerApprovalRequestDTO;
import com.example.pharmaaggregatorserver.response.ApiResponse;
import com.example.pharmaaggregatorserver.service.admin.SellerApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/sellers")
@RequiredArgsConstructor
public class AdminSellerController {

    private final SellerApprovalService approvalService;

    // ================= ADMIN SELLER REVIEW CONTROLLER =================
    /**
     * Handles admin review actions for temporary seller registrations.
     * Admin can ACCEPT, REJECT, or request CORRECTION.
     */
    @PostMapping("/review")
    public ResponseEntity<?> reviewSeller(@RequestBody SellerApprovalRequestDTO request) {
        approvalService.processReview(request);
        return ResponseEntity.ok(new ApiResponse(
                HttpStatus.OK.toString(),
                "Seller review processed successfully",
                "Seller review processed successfully"));
    }
}