package com.example.pharmaaggregatorserver.repository.buyer;

import com.example.pharmaaggregatorserver.entity.buyer.BuyerLoginOtp;
import com.example.pharmaaggregatorserver.entity.buyer.BuyerUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BuyerLoginOtpRepository extends JpaRepository<BuyerLoginOtp, Long> {

    // Find the latest active (unused, unexpired, unlocked) OTP for a buyer
    @Query("""
            SELECT o FROM BuyerLoginOtp o
            WHERE o.buyerUser = :buyerUser
              AND o.isUsed = false
              AND o.isLocked = false
              AND o.expiresAt > CURRENT_TIMESTAMP
            ORDER BY o.createdAt DESC
            """)
    Optional<BuyerLoginOtp> findActiveOtpByBuyerUser(@Param("buyerUser") BuyerUser buyerUser);

    // Invalidate all previous OTPs for a buyer (called before issuing a new one)
    @Modifying
    @Query("""
            UPDATE BuyerLoginOtp o
            SET o.isUsed = true
            WHERE o.buyerUser = :buyerUser
              AND o.isUsed = false
            """)
    void invalidateAllOtpsForBuyerUser(@Param("buyerUser") BuyerUser buyerUser);

    // Increment failed attempts for a specific OTP
    @Modifying
    @Query("""
            UPDATE BuyerLoginOtp o
            SET o.failedAttempts = o.failedAttempts + 1
            WHERE o.otpId = :otpId
            """)
    void incrementFailedAttempts(@Param("otpId") Long otpId);

    // Lock an OTP
    @Modifying
    @Query("""
            UPDATE BuyerLoginOtp o
            SET o.isLocked = true
            WHERE o.otpId = :otpId
            """)
    void lockOtp(@Param("otpId") Long otpId);

    // Mark OTP as used
    @Modifying
    @Query("""
            UPDATE BuyerLoginOtp o
            SET o.isUsed = true
            WHERE o.otpId = :otpId
            """)
    void markOtpAsUsed(@Param("otpId") Long otpId);
}
