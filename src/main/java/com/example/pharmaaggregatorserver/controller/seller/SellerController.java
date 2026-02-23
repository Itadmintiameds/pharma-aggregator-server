package com.example.pharmaaggregatorserver.controller.seller;

import com.example.pharmaaggregatorserver.dto.auth.ResetPassqordDTO;
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
}
