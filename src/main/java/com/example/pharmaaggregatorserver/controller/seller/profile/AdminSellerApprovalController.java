package com.example.pharmaaggregatorserver.controller.seller.profile;
import com.example.pharmaaggregatorserver.dto.seller.SellerLogIn.ApiResponse;
import com.example.pharmaaggregatorserver.dto.seller.profile.ApprovalRequestDTO;
import com.example.pharmaaggregatorserver.dto.seller.profile.PendingSellerResponseDTO;
import com.example.pharmaaggregatorserver.service.profile.SellerProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/seller-requests")
@RequiredArgsConstructor
public class AdminSellerApprovalController {

    private final SellerProfileService sellerProfileService;

    @GetMapping("/pending")
    public ResponseEntity<List<PendingSellerResponseDTO>> getPendingRequests() {
        List<PendingSellerResponseDTO> pendingRequests = sellerProfileService.getPendingRequests();
        return ResponseEntity.ok(pendingRequests);
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<String>> approveRequest(
            @PathVariable Long id,
            @RequestParam String approvedBy) {
        sellerProfileService.approveSellerUpdate(id, approvedBy);

        ApiResponse<String> response = new ApiResponse<>(
                "SUCCESS",
                "Request approved successfully and pending data has been moved to main tables",
                null
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<String>> rejectRequest(
            @PathVariable Long id,
            @RequestBody ApprovalRequestDTO request) {
        sellerProfileService.rejectSellerUpdate(id, request.getRejectionReason(), request.getApprovedBy());

        ApiResponse<String> response = new ApiResponse<>(
                "SUCCESS",
                "Request rejected successfully",
                null
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/batch-approve")
    public ResponseEntity<String> approveMultipleRequests(
            @RequestBody List<Long> pendingSellerIds,
            @RequestParam String approvedBy) {
        sellerProfileService.approveAndDeleteAll(pendingSellerIds, approvedBy);
        return ResponseEntity.ok("All requests approved successfully");
    }
}