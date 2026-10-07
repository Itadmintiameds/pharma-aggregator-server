package com.example.pharmaaggregatorserver.repository.seller.SellerLogIn;

import com.example.pharmaaggregatorserver.entity.auth.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface refreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);
}
