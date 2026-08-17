package com.example.pharmaaggregatorserver.controller.buyer;

import com.example.pharmaaggregatorserver.dto.buyer.BuyerProfileResponseDTO;
import com.example.pharmaaggregatorserver.response.ApiResponse;
import com.example.pharmaaggregatorserver.service.buyer.BuyerProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/buyer/profile")
@RequiredArgsConstructor
public class BuyerProfileController {

    private final BuyerProfileService buyerProfileService;

    // GET /buyer/profile/by-user/{buyerUserId} — resolves the approved Buyer's
    // business ID from the logged-in BuyerUser's own ID, since the login
    // response only ever carries buyerUserId (see BuyerProfileService).
    @GetMapping("/by-user/{buyerUserId}")
    public ResponseEntity<ApiResponse<BuyerProfileResponseDTO>> getByUserId(@PathVariable Long buyerUserId) {
        BuyerProfileResponseDTO response = buyerProfileService.getByUserId(buyerUserId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.toString(), "Buyer profile fetched successfully", response));
    }
}
