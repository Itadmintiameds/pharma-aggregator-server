package com.example.pharmaaggregatorserver.repository.buyer;

import com.example.pharmaaggregatorserver.entity.buyer.BuyerRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BuyerRefreshTokenRepository extends JpaRepository<BuyerRefreshToken, Long> {
    Optional<BuyerRefreshToken> findByTokenHash(String tokenHash);
}
