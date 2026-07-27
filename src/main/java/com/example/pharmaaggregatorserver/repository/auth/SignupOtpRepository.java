package com.example.pharmaaggregatorserver.repository.auth;

import com.example.pharmaaggregatorserver.entity.auth.SignupOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SignupOtpRepository extends JpaRepository<SignupOtp, Long> {
    Optional<SignupOtp> findTopByEmailOrderByExpiryTimeDesc(String email);
}
