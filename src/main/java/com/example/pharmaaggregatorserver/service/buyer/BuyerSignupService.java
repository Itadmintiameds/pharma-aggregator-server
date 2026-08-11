package com.example.pharmaaggregatorserver.service.buyer;

import com.example.pharmaaggregatorserver.dto.buyer.BuyerOtpSentResponse;
import com.example.pharmaaggregatorserver.dto.buyer.BuyerSignupOtpVerifyRequest;
import com.example.pharmaaggregatorserver.dto.buyer.BuyerSignupRequest;
import com.example.pharmaaggregatorserver.entity.buyer.BuyerSignupOtp;
import com.example.pharmaaggregatorserver.entity.buyer.BuyerUser;
import com.example.pharmaaggregatorserver.exception.ApplicationException;
import com.example.pharmaaggregatorserver.repository.buyer.BuyerSignupOtpRepository;
import com.example.pharmaaggregatorserver.repository.buyer.BuyerUserRepository;
import com.example.pharmaaggregatorserver.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * Standalone "signup first" flow for buyers: email + phone + password up
 * front, only the email gets OTP-verified before the BuyerUser row is
 * created. Phone is stored but isPhoneVerified stays false until a later
 * step (org-details/purchase flow, not part of this build). Fully isolated
 * from the seller signup stack — no shared tables or services.
 */
@Service
@RequiredArgsConstructor
public class BuyerSignupService {

    private static final int OTP_EXPIRY_MINUTES = 5;

    private final BuyerSignupOtpRepository buyerSignupOtpRepository;
    private final BuyerUserRepository buyerUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Transactional
    public BuyerOtpSentResponse sendSignupOtp(BuyerSignupRequest request) {
        String email = request.getEmail();

        if (buyerUserRepository.existsByEmail(email)) {
            throw new ApplicationException(
                    HttpStatus.CONFLICT,
                    "An account already exists for email: " + email
            );
        }

        String otp = String.valueOf(100000 + new Random().nextInt(900000));
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        BuyerSignupOtp signupOtp = BuyerSignupOtp.builder()
                .email(email)
                .phone(request.getPhone())
                .otp(otp)
                .passwordHash(hashedPassword)
                .expiryTime(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .verified(false)
                .createdAt(LocalDateTime.now())
                .build();
        buyerSignupOtpRepository.save(signupOtp);

        emailService.sendBuyerOtp(email, otp);

        return BuyerOtpSentResponse.builder()
                .message("OTP sent to " + email + ". Valid for " + OTP_EXPIRY_MINUTES + " minutes.")
                .username(email)
                .build();
    }

    @Transactional
    public BuyerOtpSentResponse verifyAndCreateBuyer(BuyerSignupOtpVerifyRequest request) {
        String email = request.getEmail();

        BuyerSignupOtp signupOtp = buyerSignupOtpRepository.findTopByEmailOrderByExpiryTimeDesc(email)
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
        buyerSignupOtpRepository.save(signupOtp);

        if (buyerUserRepository.existsByEmail(email)) {
            throw new ApplicationException(
                    HttpStatus.CONFLICT,
                    "A buyer account already exists for email: " + email
            );
        }

        BuyerUser buyerUser = new BuyerUser();
        buyerUser.setEmail(email);
        buyerUser.setPhone(signupOtp.getPhone());
        buyerUser.setPasswordHash(signupOtp.getPasswordHash());
        buyerUser.setEmailVerified(true);
        buyerUser.setPhoneVerified(false);
        buyerUser.setActive(true);
        buyerUser.setAccountLocked(false);
        buyerUser.setFailedLoginAttempts(0);
        buyerUserRepository.save(buyerUser);

        return BuyerOtpSentResponse.builder()
                .message("Account created successfully. Please log in.")
                .username(email)
                .build();
    }
}
