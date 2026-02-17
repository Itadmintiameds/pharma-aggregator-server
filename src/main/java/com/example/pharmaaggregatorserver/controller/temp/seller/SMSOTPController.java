package com.example.pharmaaggregatorserver.controller.temp.seller;

import com.example.pharmaaggregatorserver.dto.seller.SMSOtpRequestDTO;
import com.example.pharmaaggregatorserver.dto.seller.SMSOtpResponseDTO;
import com.example.pharmaaggregatorserver.dto.seller.SMSVerifyOtpRequestDTO;
import com.example.pharmaaggregatorserver.service.temp.seller.TwilioOTPService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/otp")
public class SMSOTPController {

    private final TwilioOTPService otpService;

    public SMSOTPController(TwilioOTPService otpService) {
        this.otpService = otpService;
    }

    @PostMapping("/send")
    public ResponseEntity<SMSOtpResponseDTO> sendOtp(
            @RequestBody SMSOtpRequestDTO request) {
        return ResponseEntity.ok(otpService.sendOTP(request));
    }

    @PostMapping("/verify")
    public ResponseEntity<SMSOtpResponseDTO> verify(
            @RequestBody SMSVerifyOtpRequestDTO dto) {

        SMSOtpResponseDTO response = otpService.verifyOTP(dto);

        return ResponseEntity.ok(response);
    }

}

