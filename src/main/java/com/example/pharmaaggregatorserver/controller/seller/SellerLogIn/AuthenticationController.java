package com.example.pharmaaggregatorserver.controller.seller.SellerLogIn;

import com.example.pharmaaggregatorserver.dto.seller.SellerLogIn.LoginRequest;
import com.example.pharmaaggregatorserver.dto.seller.SellerLogIn.LoginResponse;
import com.example.pharmaaggregatorserver.dto.seller.SellerLogIn.OtpSentResponse;
import com.example.pharmaaggregatorserver.dto.seller.SellerLogIn.OtpVerificationRequest;
import com.example.pharmaaggregatorserver.exception.*;
import com.example.pharmaaggregatorserver.exception.auth.OtpExpiredException;
import com.example.pharmaaggregatorserver.exception.auth.OtpInvalidException;
import com.example.pharmaaggregatorserver.exception.auth.OtpLockedException;
import com.example.pharmaaggregatorserver.service.seller.SellerLogIn.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/authentication")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthenticationController {

    private final AuthService authService;

    // ─────────────────────────────────────────────────────────
    // STEP 1 — POST /authentication/login
    //          Validates username + password, sends OTP to email
    // ─────────────────────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            OtpSentResponse response = authService.validateCredentialsAndSendOtp(loginRequest);
            return ResponseEntity.ok(response);
        } catch (InvalidCredentialsException e) {
            return buildErrorResponse(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (AccountLockedException e) {
            return buildErrorResponse(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (AccountInactiveException e) {
            return buildErrorResponse(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred during login");
        }
    }

    // ─────────────────────────────────────────────────────────
    // STEP 2 — POST /authentication/verify-otp
    //          Validates OTP, returns JWT token
    // ─────────────────────────────────────────────────────────
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody OtpVerificationRequest otpRequest) {
        try {
            LoginResponse response = authService.verifyOtpAndIssueToken(otpRequest);
            return ResponseEntity.ok(response);
        } catch (OtpInvalidException e) {
            return buildErrorResponse(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (OtpExpiredException e) {
            return buildErrorResponse(HttpStatus.GONE, e.getMessage());           // 410 Gone — OTP expired
        } catch (OtpLockedException e) {
            return buildErrorResponse(HttpStatus.TOO_MANY_REQUESTS, e.getMessage()); // 429
        } catch (AccountLockedException e) {
            return buildErrorResponse(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (AccountInactiveException e) {
            return buildErrorResponse(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (InvalidCredentialsException e) {
            return buildErrorResponse(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (Exception e) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred during OTP verification");
        }
    }

    // ─────────────────────────────────────────────────────────
    // POST /authentication/logout
    // JWT is stateless — client drops the token.
    // ─────────────────────────────────────────────────────────
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }

    // ─────────────────────────────────────────────────────────
    // Shared error builder
    // ─────────────────────────────────────────────────────────
    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", System.currentTimeMillis());
        response.put("status", status.value());
        response.put("error", status.getReasonPhrase());
        response.put("message", message);
        return ResponseEntity.status(status).body(response);
    }
}