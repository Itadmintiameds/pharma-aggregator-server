package com.example.pharmaaggregatorserver.controller.admin;

import com.example.pharmaaggregatorserver.dto.buyer.BuyerApprovalRequestDTO;
import com.example.pharmaaggregatorserver.dto.buyer.BuyerApprovalResultDTO;
import com.example.pharmaaggregatorserver.response.ApiResponse;
import com.example.pharmaaggregatorserver.service.admin.BuyerApprovalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/buyers")
@RequiredArgsConstructor
public class AdminBuyerController {

    private final BuyerApprovalService approvalService;

    /**
     * Handles admin review actions for temporary buyer registrations.
     * Admin can ACCEPT, REJECT, or request CORRECTION.
     */
    @PostMapping("/review")
    public ResponseEntity<?> reviewBuyer(@Valid @RequestBody BuyerApprovalRequestDTO request) {
        BuyerApprovalResultDTO result = approvalService.processReview(request);
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.toString(),
                "Buyer review processed successfully",
                result));
    }
}
