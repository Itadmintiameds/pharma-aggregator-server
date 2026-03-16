package com.example.pharmaaggregatorserver.controller.seller.profile;

import com.example.pharmaaggregatorserver.dto.seller.profile.SellerEditRequest;
import com.example.pharmaaggregatorserver.dto.seller.profile.SellerResponseDTO;
import com.example.pharmaaggregatorserver.entity.seller.Seller;
import com.example.pharmaaggregatorserver.response.ApiResponse;
import com.example.pharmaaggregatorserver.service.profile.SellerProfileService;
import com.example.pharmaaggregatorserver.service.seller.SellerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sellers")
@RequiredArgsConstructor
public class SellerProfileController {

    private final SellerProfileService sellerprofileService;
    private final SellerService sellerService;

    @PutMapping("/{sellerId}/request-update")
    public ResponseEntity<SellerResponseDTO> requestSellerUpdate(
            @PathVariable String sellerId,
            @Valid @RequestBody SellerEditRequest request,
            @RequestParam String requestedBy) {

        SellerResponseDTO response = sellerprofileService.requestSellerUpdate(sellerId, request, requestedBy);
        return ResponseEntity.ok(response);
    }
    // Find Seller by User ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> findSellerByUserId(@PathVariable Long userId) {
        Seller seller = sellerService.findSellerByUserId(userId);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Seller found", seller));
    }
}