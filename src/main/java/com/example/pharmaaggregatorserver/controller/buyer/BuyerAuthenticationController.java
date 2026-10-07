package com.example.pharmaaggregatorserver.controller.buyer;

import com.example.pharmaaggregatorserver.dto.buyer.BuyerLoginRequest;
import com.example.pharmaaggregatorserver.dto.buyer.BuyerLoginResponse;
import com.example.pharmaaggregatorserver.dto.buyer.BuyerLogoutRequest;
import com.example.pharmaaggregatorserver.dto.buyer.BuyerOtpSentResponse;
import com.example.pharmaaggregatorserver.dto.buyer.BuyerOtpVerificationRequest;
import com.example.pharmaaggregatorserver.dto.buyer.BuyerRefreshRequest;
import com.example.pharmaaggregatorserver.dto.buyer.BuyerResetPasswordRequest;
import com.example.pharmaaggregatorserver.dto.buyer.BuyerUserSummaryDTO;
import com.example.pharmaaggregatorserver.exception.*;
import com.example.pharmaaggregatorserver.exception.auth.OtpExpiredException;
import com.example.pharmaaggregatorserver.exception.auth.OtpInvalidException;
import com.example.pharmaaggregatorserver.exception.auth.OtpLockedException;
import com.example.pharmaaggregatorserver.exception.auth.RefreshTokenException;
import com.example.pharmaaggregatorserver.security.UserDetailsImpl;
import com.example.pharmaaggregatorserver.service.buyer.BuyerAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/buyer/authentication")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class BuyerAuthenticationController {

    private final BuyerAuthService buyerAuthService;

    // ─────────────────────────────────────────────────────────
    // STEP 1 — POST /buyer/authentication/login
    //          Validates username + password, sends OTP to email
    // ─────────────────────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody BuyerLoginRequest loginRequest) {
        try {
            BuyerOtpSentResponse response = buyerAuthService.validateCredentialsAndSendOtp(loginRequest);
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
    // STEP 2 — POST /buyer/authentication/verify-otp
    //          Validates OTP, returns JWT token
    // ─────────────────────────────────────────────────────────
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody BuyerOtpVerificationRequest otpRequest) {
        try {
            BuyerLoginResponse response = buyerAuthService.verifyOtpAndIssueToken(otpRequest);
            return ResponseEntity.ok(response);
        } catch (OtpInvalidException e) {
            return buildErrorResponse(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (OtpExpiredException e) {
            return buildErrorResponse(HttpStatus.GONE, e.getMessage());
        } catch (OtpLockedException e) {
            return buildErrorResponse(HttpStatus.TOO_MANY_REQUESTS, e.getMessage());
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

    // POST /buyer/authentication/refresh
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody BuyerRefreshRequest request) {
        try {
            BuyerLoginResponse response = buyerAuthService.refreshAccessToken(request.getRefreshToken());
            return ResponseEntity.ok(response);
        } catch (RefreshTokenException e) {
            return buildErrorResponse(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }

    // POST /buyer/authentication/reset-password — first-time password reset
    // for a buyer account still holding a temporary password (see
    // BuyerAuthService.resetPassword). Only usable while passwordTemporary
    // is true; anything else is rejected as a BAD_REQUEST ApplicationException.
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody BuyerResetPasswordRequest request) {
        try {
            buyerAuthService.resetPassword(request.getEmail(), request.getNewPassword());
            return ResponseEntity.ok(new com.example.pharmaaggregatorserver.response.ApiResponse<>(
                    "SUCCESS", "Password changed successfully", null));
        } catch (InvalidCredentialsException e) {
            return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (AccountLockedException | AccountInactiveException e) {
            return buildErrorResponse(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (ApplicationException e) {
            return buildErrorResponse(e.getStatus(), e.getMessage());
        }
    }

    // GET /buyer/authentication/me — refreshes the logged-in buyer's own
    // email/phone from the DB, for callers that shouldn't rely solely on
    // whatever was cached in localStorage at the last login/OTP-verify.
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl user)) {
            return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        BuyerUserSummaryDTO response = buyerAuthService.getCurrentUserSummary(user.getId());
        return ResponseEntity.ok(response);
    }

    // POST /buyer/authentication/logout
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody BuyerLogoutRequest request) {
        buyerAuthService.logout(request.getRefreshToken());
        return ResponseEntity.ok(Map.of("message", "Logout successful"));
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
