package com.example.pharmaaggregatorserver.controller.seller;

import com.example.pharmaaggregatorserver.dto.auth.ResetPassqordDTO;
import com.example.pharmaaggregatorserver.entity.seller.Seller;
import com.example.pharmaaggregatorserver.response.ApiResponse;
import com.example.pharmaaggregatorserver.service.seller.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sellers")
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;

    //    Reset User Password
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPassqordDTO dto) {
        sellerService.resetPassword(dto.getUsername(), dto.getCurrentPassword(),  dto.getNewPassword());
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Password Changed", null));
    }

    // Find Seller by User ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> findSellerByUserId(@PathVariable Long userId) {
        Seller seller = sellerService.findSellerByUserId(userId);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Seller found", seller));
    }

}
