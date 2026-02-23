package com.example.pharmaaggregatorserver.controller.seller;

import com.example.pharmaaggregatorserver.response.ApiResponse;
import com.example.pharmaaggregatorserver.service.seller.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sellers")
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;

    //    Reset User Password
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String username,
                                           @RequestParam String currentPassword,
                                           @RequestParam String newPassword) {
        sellerService.resetPassword(username, currentPassword, newPassword);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Password Changed", null));
    }
}
