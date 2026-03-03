package com.example.pharmaaggregatorserver.controller.seller.SellerLogIn;

// AuthController.java
import com.example.pharmaaggregatorserver.dto.auth.*;
import com.example.pharmaaggregatorserver.dto.seller.SellerLogIn.*;
import com.example.pharmaaggregatorserver.dto.seller.SellerLogIn.ApiResponse;
import com.example.pharmaaggregatorserver.service.seller.SellerLogIn.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    // Reset Password with Current Password
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request.getUsername(), request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Password changed successfully", null));
    }

    // Forgot Password - Request Reset Link
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        userService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(new ApiResponse<>(
                "SUCCESS",
                "If your email exists in our system, you will receive a password reset link",
                null
        ));
    }

    // Validate Reset Token
    @PostMapping("/validate-reset-token")
    public ResponseEntity<?> validateResetToken(@Valid @RequestBody ValidateTokenRequest request) {
        boolean isValid = userService.validateResetToken(request.getToken());
        if (isValid) {
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Token is valid", null));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("ERROR", "Invalid or expired token", null));
        }
    }

    // Reset Password with Token
    @PostMapping("/reset-password-with-token")
    public ResponseEntity<?> resetPasswordWithToken(@Valid @RequestBody ResetPasswordWithTokenRequest request) {
        userService.resetPasswordWithToken(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Password has been reset successfully", null));
    }
}