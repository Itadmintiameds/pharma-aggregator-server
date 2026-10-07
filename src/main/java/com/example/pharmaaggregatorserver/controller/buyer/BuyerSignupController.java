package com.example.pharmaaggregatorserver.controller.buyer;

import com.example.pharmaaggregatorserver.dto.buyer.BuyerOtpSentResponse;
import com.example.pharmaaggregatorserver.dto.buyer.BuyerSignupOtpVerifyRequest;
import com.example.pharmaaggregatorserver.dto.buyer.BuyerSignupRequest;
import com.example.pharmaaggregatorserver.exception.ApplicationException;
import com.example.pharmaaggregatorserver.service.buyer.BuyerSignupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/buyer/auth/signup")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class BuyerSignupController {

    private final BuyerSignupService buyerSignupService;

    // POST /buyer/auth/signup — email + phone + password, sends OTP
    @PostMapping
    public ResponseEntity<?> signup(@Valid @RequestBody BuyerSignupRequest request) {
        try {
            BuyerOtpSentResponse response = buyerSignupService.sendSignupOtp(request);
            return ResponseEntity.ok(response);
        } catch (ApplicationException e) {
            return buildErrorResponse(e.getStatus(), e.getMessage());
        }
    }

    // POST /buyer/auth/signup/verify-otp — verifies OTP, creates the BuyerUser (no token issued — log in separately)
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody BuyerSignupOtpVerifyRequest request) {
        try {
            BuyerOtpSentResponse response = buyerSignupService.verifyAndCreateBuyer(request);
            return ResponseEntity.ok(response);
        } catch (ApplicationException e) {
            return buildErrorResponse(e.getStatus(), e.getMessage());
        }
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", System.currentTimeMillis());
        response.put("status", status.value());
        response.put("error", status.getReasonPhrase());
        response.put("message", message);
        return ResponseEntity.status(status).body(response);
    }
}
