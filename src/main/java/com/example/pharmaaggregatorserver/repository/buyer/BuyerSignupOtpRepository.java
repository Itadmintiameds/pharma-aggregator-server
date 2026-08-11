package com.example.pharmaaggregatorserver.repository.buyer;

import com.example.pharmaaggregatorserver.entity.buyer.BuyerSignupOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BuyerSignupOtpRepository extends JpaRepository<BuyerSignupOtp, Long> {
    Optional<BuyerSignupOtp> findTopByEmailOrderByExpiryTimeDesc(String email);
}
