package com.example.pharmaaggregatorserver.repository.temp.seller;

import com.example.pharmaaggregatorserver.entity.temp.seller.PhoneOTP;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PhoneOTPRepository extends JpaRepository<PhoneOTP, Long> {

    Optional<PhoneOTP> findTopByPhoneOrderByExpiryTimeDesc(String phone);

}