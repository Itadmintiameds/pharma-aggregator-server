package com.example.pharmaaggregatorserver.repository.auth;

import com.example.pharmaaggregatorserver.entity.auth.LoginOtp;
import com.example.pharmaaggregatorserver.entity.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoginOtpRepository extends JpaRepository<LoginOtp, Long> {

    // Find the latest active (unused, unexpired, unlocked) OTP for a user
    @Query("""
            SELECT o FROM LoginOtp o
            WHERE o.user = :user
              AND o.isUsed = false
              AND o.isLocked = false
              AND o.expiresAt > CURRENT_TIMESTAMP
            ORDER BY o.createdAt DESC
            """)
    Optional<LoginOtp> findActiveOtpByUser(@Param("user") User user);

    // Invalidate all previous OTPs for a user (called before issuing a new one)
    @Modifying
    @Query("""
            UPDATE LoginOtp o
            SET o.isUsed = true
            WHERE o.user = :user
              AND o.isUsed = false
            """)
    void invalidateAllOtpsForUser(@Param("user") User user);

    // Increment failed attempts for a specific OTP
    @Modifying
    @Query("""
            UPDATE LoginOtp o
            SET o.failedAttempts = o.failedAttempts + 1
            WHERE o.otpId = :otpId
            """)
    void incrementFailedAttempts(@Param("otpId") Long otpId);

    // Lock an OTP
    @Modifying
    @Query("""
            UPDATE LoginOtp o
            SET o.isLocked = true
            WHERE o.otpId = :otpId
            """)
    void lockOtp(@Param("otpId") Long otpId);

    // Mark OTP as used
    @Modifying
    @Query("""
            UPDATE LoginOtp o
            SET o.isUsed = true
            WHERE o.otpId = :otpId
            """)
    void markOtpAsUsed(@Param("otpId") Long otpId);
}