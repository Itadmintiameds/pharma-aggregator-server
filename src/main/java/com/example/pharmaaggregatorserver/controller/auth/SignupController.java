package com.example.pharmaaggregatorserver.controller.auth;

import com.example.pharmaaggregatorserver.dto.auth.SignupOtpVerifyRequest;
import com.example.pharmaaggregatorserver.dto.auth.SignupRequest;
import com.example.pharmaaggregatorserver.dto.seller.SellerLogIn.OtpSentResponse;
import com.example.pharmaaggregatorserver.exception.ApplicationException;
import com.example.pharmaaggregatorserver.service.auth.SignupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth/signup")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class SignupController {

    private final SignupService signupService;

    // POST /auth/signup — email + password, sends OTP
    @PostMapping
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest request) {
        try {
            OtpSentResponse response = signupService.sendSignupOtp(request);
            return ResponseEntity.ok(response);
        } catch (ApplicationException e) {
            return buildErrorResponse(e.getStatus(), e.getMessage());
        }
    }

    // POST /auth/signup/verify-otp — verifies OTP, creates the User (no token issued — log in separately)
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody SignupOtpVerifyRequest request) {
        try {
            OtpSentResponse response = signupService.verifyAndCreateUser(request);
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
