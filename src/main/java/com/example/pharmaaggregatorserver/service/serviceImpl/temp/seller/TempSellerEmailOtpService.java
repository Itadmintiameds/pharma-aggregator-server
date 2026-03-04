package com.example.pharmaaggregatorserver.service.serviceImpl.temp.seller;


import com.example.pharmaaggregatorserver.dto.*;
import com.example.pharmaaggregatorserver.dto.seller.EmailOtpSendRequestDTO;
import com.example.pharmaaggregatorserver.dto.seller.EmailOtpVerifyRequestDTO;
import com.example.pharmaaggregatorserver.dto.seller.OtpResponseDTO;
import com.example.pharmaaggregatorserver.entity.temp.seller.*;
import com.example.pharmaaggregatorserver.repository.temp.seller.*;
import com.example.pharmaaggregatorserver.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class TempSellerEmailOtpService {

    private final TempSellerEmailOtpRepository otpRepository;
    private final EmailService emailService;

    // ================== SEND OTP ==================
    public OtpResponseDTO sendOtp(EmailOtpSendRequestDTO request) {

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new RuntimeException("Email is required");
        }

        String otp = String.valueOf(
                100000 + new Random().nextInt(900000)
        );

        TempSellerEmailOtp emailOtp = TempSellerEmailOtp.builder()
                .email(request.getEmail())
                .otp(otp)
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .verified(false)
                .build();

        otpRepository.save(emailOtp);

        emailService.sendCoordinatorOtp(request.getEmail(), otp);

        return OtpResponseDTO.builder()
                .status("SUCCESS")
                .message("OTP sent successfully")
                .build();
    }
    // ================== SEND OTP ==================
    public OtpResponseDTO sendOtpExistedemail(EmailOtpSendRequestDTO request) {
        String otp = String.valueOf(
                100000 + new Random().nextInt(900000)
        );

        TempSellerEmailOtp emailOtp = TempSellerEmailOtp.builder()
                .email(request.getEmail())
                .otp(otp)
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .verified(false)
                .build();

        otpRepository.save(emailOtp);

        emailService.sendCoordinatorOtp(request.getEmail(), otp);

        return OtpResponseDTO.builder()
                .status("SUCCESS")
                .message("OTP sent successfully")
                .build();
    }

    // ================== VERIFY OTP ==================
    public OtpResponseDTO verifyOtp(EmailOtpVerifyRequestDTO request) {

        TempSellerEmailOtp otp = otpRepository
                .findTopByEmailOrderByExpiryTimeDesc(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("OTP not found"));

        if (otp.isVerified()) {
            throw new RuntimeException("OTP already used");
        }

        if (otp.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired");
        }

        if (!otp.getOtp().equals(request.getOtp())) {
            throw new RuntimeException("Invalid OTP");
        }

        otp.setVerified(true);
        otpRepository.save(otp);

        return OtpResponseDTO.builder()
                .status("SUCCESS")
                .message("Email verified successfully")
                .build();
    }
}
