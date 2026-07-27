package com.example.pharmaaggregatorserver.service.auth;

import com.example.pharmaaggregatorserver.dto.auth.SignupOtpVerifyRequest;
import com.example.pharmaaggregatorserver.dto.auth.SignupRequest;
import com.example.pharmaaggregatorserver.dto.seller.SellerLogIn.OtpSentResponse;
import com.example.pharmaaggregatorserver.entity.auth.SignupOtp;
import com.example.pharmaaggregatorserver.exception.ApplicationException;
import com.example.pharmaaggregatorserver.repository.auth.SignupOtpRepository;
import com.example.pharmaaggregatorserver.repository.auth.UserRepository;
import com.example.pharmaaggregatorserver.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * Standalone "signup first" flow: a seller creates a login (email + password)
 * before filling in any company/registration details. The User row is only
 * created once the email is OTP-verified — until then, the (hashed) password
 * and OTP live in SignupOtp. No token is issued here: the seller must log in
 * explicitly afterward through the normal login+OTP flow.
 */
@Service
@RequiredArgsConstructor
public class SignupService {

    private static final int OTP_EXPIRY_MINUTES = 5;

    private final SignupOtpRepository signupOtpRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final UserCreationService userCreationService;

    @Transactional
    public OtpSentResponse sendSignupOtp(SignupRequest request) {
        String email = request.getEmail();

        if (userRepository.existsByUsername(email)) {
            throw new ApplicationException(
                    HttpStatus.CONFLICT,
                    "An account already exists for email: " + email
            );
        }

        String otp = String.valueOf(100000 + new Random().nextInt(900000));
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        SignupOtp signupOtp = SignupOtp.builder()
                .email(email)
                .otp(otp)
                .passwordHash(hashedPassword)
                .expiryTime(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .verified(false)
                .createdAt(LocalDateTime.now())
                .build();
        signupOtpRepository.save(signupOtp);

        emailService.sendCoordinatorOtp(email, otp);

        return OtpSentResponse.builder()
                .message("OTP sent to " + email + ". Valid for " + OTP_EXPIRY_MINUTES + " minutes.")
                .username(email)
                .build();
    }

    @Transactional
    public OtpSentResponse verifyAndCreateUser(SignupOtpVerifyRequest request) {
        String email = request.getEmail();

        SignupOtp signupOtp = signupOtpRepository.findTopByEmailOrderByExpiryTimeDesc(email)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "OTP not found. Please sign up again."));

        if (signupOtp.isVerified()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "OTP already used. Please sign up again.");
        }

        if (signupOtp.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "OTP expired. Please sign up again.");
        }

        if (!signupOtp.getOtp().equals(request.getOtp())) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Invalid OTP.");
        }

        signupOtp.setVerified(true);
        signupOtpRepository.save(signupOtp);

        userCreationService.createUserFromSignup(email, signupOtp.getPasswordHash());

        return OtpSentResponse.builder()
                .message("Account created successfully. Please log in.")
                .username(email)
                .build();
    }
}
