package com.example.pharmaaggregatorserver.controller.temp.seller;


import com.example.pharmaaggregatorserver.dto.*;
import com.example.pharmaaggregatorserver.dto.seller.EmailOtpSendRequestDTO;
import com.example.pharmaaggregatorserver.dto.seller.EmailOtpVerifyRequestDTO;
import com.example.pharmaaggregatorserver.dto.seller.OtpResponseDTO;
import com.example.pharmaaggregatorserver.service.serviceImpl.temp.seller.TempSellerEmailOtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/temp-seller/email-otp")
@RequiredArgsConstructor
public class TempSellerEmailOtpController {

    private final TempSellerEmailOtpService otpService;

    @PostMapping("/send")
    public OtpResponseDTO sendOtp(
            @RequestBody EmailOtpSendRequestDTO request) {
        return otpService.sendOtp(request);
    }

    @PostMapping("/verify")
    public OtpResponseDTO verifyOtp(
            @RequestBody EmailOtpVerifyRequestDTO request) {
        return otpService.verifyOtp(request);
    }
}
