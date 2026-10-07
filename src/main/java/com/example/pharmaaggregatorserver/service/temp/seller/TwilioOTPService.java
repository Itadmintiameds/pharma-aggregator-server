package com.example.pharmaaggregatorserver.service.temp.seller;


import com.example.pharmaaggregatorserver.dto.seller.SMSOtpRequestDTO;
import com.example.pharmaaggregatorserver.dto.seller.SMSOtpResponseDTO;
import com.example.pharmaaggregatorserver.dto.seller.SMSVerifyOtpRequestDTO;
import com.example.pharmaaggregatorserver.entity.temp.seller.PhoneOTP;
import com.example.pharmaaggregatorserver.repository.temp.seller.PhoneOTPRepository;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TwilioOTPService {

    private final PhoneOTPRepository otpRepository;

    @Value("${twilio.verify.service-sid}")
    private String serviceSid;

    // ================= SEND OTP =================
    public SMSOtpResponseDTO sendOTP(SMSOtpRequestDTO request) {

        if (request.getPhone() == null || request.getPhone().isBlank()) {
            throw new RuntimeException("Phone number is required");
        }

        // Send OTP using Twilio Verify
        Verification.creator(
                serviceSid,
                request.getPhone(),
                "sms"
        ).create();

        // Save record in DB (no need to store OTP because Twilio handles it)
        PhoneOTP phoneOTP = PhoneOTP.builder()
                .phone(request.getPhone())
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .verified(false)
                .build();

        otpRepository.save(phoneOTP);

        return SMSOtpResponseDTO.builder()
                .status("SUCCESS")
                .message("OTP sent successfully")
                .build();
    }

    // ================= VERIFY OTP =================
//    public SMSOtpResponseDTO verifyOTP(SMSVerifyOtpRequestDTO request) {
//
//        PhoneOTP phoneOTP = otpRepository
//                .findTopByPhoneOrderByExpiryTimeDesc(request.getPhone())
//                .orElseThrow(() ->
//                        new ResponseStatusException(
//                                HttpStatus.NOT_FOUND,
//                                "OTP request not found"
//                        )
//                );
//
//        if (phoneOTP.isVerified()) {
//            throw new ResponseStatusException(
//                    HttpStatus.BAD_REQUEST,
//                    "OTP already verified"
//            );
//        }
//
//        if (phoneOTP.getExpiryTime().isBefore(LocalDateTime.now())) {
//            throw new ResponseStatusException(
//                    HttpStatus.BAD_REQUEST,
//                    "OTP expired"
//            );
//        }
//
//        VerificationCheck verificationCheck =
//                VerificationCheck.creator(serviceSid)
//                        .setTo(request.getPhone())
//                        .setCode(request.getOtp())
//                        .create();
//
//        if (!"approved".equalsIgnoreCase(verificationCheck.getStatus())) {
//            throw new ResponseStatusException(
//                    HttpStatus.BAD_REQUEST,
//                    "Invalid OTP"
//            );
//        }
//
//        phoneOTP.setVerified(true);
//        otpRepository.save(phoneOTP);
//
//        return SMSOtpResponseDTO.builder()
//                .status("SUCCESS")
//                .message("OTP verified successfully")
//                .build();
//    }
    public SMSOtpResponseDTO verifyOTP(SMSVerifyOtpRequestDTO request) {

        PhoneOTP phoneOTP = otpRepository
                .findTopByPhoneOrderByExpiryTimeDesc(request.getPhone())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "OTP request not found"
                ));

        if (phoneOTP.isVerified()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP already verified");
        }

        if (phoneOTP.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP expired");
        }

        try {
            VerificationCheck verificationCheck =
                    VerificationCheck.creator(serviceSid)
                            .setTo(request.getPhone())
                            .setCode(request.getOtp())
                            .create();

            if (!"approved".equalsIgnoreCase(verificationCheck.getStatus())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid OTP"
                );
            }
//            if (!"approved".equalsIgnoreCase(verificationCheck.getStatus())) {
//                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid OTP");
//            }

        } catch (Exception e) {
            // IMPORTANT FIX HERE
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid OTP"
            );
        }

        phoneOTP.setVerified(true);
        otpRepository.save(phoneOTP);

        return SMSOtpResponseDTO.builder()
                .status("SUCCESS")
                .message("OTP verified successfully")
                .build();
    }
}