package com.example.pharmaaggregatorserver.repository.temp.seller;

import com.example.pharmaaggregatorserver.entity.temp.seller.TempSellerCoordinator;
import com.example.pharmaaggregatorserver.entity.temp.seller.TempSellerEmailOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TempSellerEmailOtpRepository
        extends JpaRepository<TempSellerEmailOtp, Long> {
    Optional<TempSellerEmailOtp> findByEmail(String email);
    Optional<TempSellerEmailOtp>
    findTopByEmailOrderByExpiryTimeDesc(String email);

}