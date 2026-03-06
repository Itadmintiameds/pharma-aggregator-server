package com.example.pharmaaggregatorserver.controller.seller.SellerLogIn;

import com.example.pharmaaggregatorserver.dto.seller.SellerLogIn.LoginRequest;
import com.example.pharmaaggregatorserver.dto.seller.SellerLogIn.LoginResponse;
import com.example.pharmaaggregatorserver.exception.AccountInactiveException;
import com.example.pharmaaggregatorserver.exception.AccountLockedException;
import com.example.pharmaaggregatorserver.exception.InvalidCredentialsException;

import com.example.pharmaaggregatorserver.service.seller.SellerLogIn.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthenticationController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = authService.authenticateUser(loginRequest);
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

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // In JWT-based authentication, logout is handled client-side by removing the token
        // You can implement token blacklisting if needed
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
