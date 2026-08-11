package com.example.pharmaaggregatorserver.entity.buyer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "tbl_buyer_login_otp")
public class BuyerLoginOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "otp_id")
    private Long otpId;

    // FK → tbl_buyer_user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_user_id", nullable = false)
    @JsonIgnore
    private BuyerUser buyerUser;

    @Column(name = "otp_code", nullable = false, length = 6)
    private String otpCode;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    // Prevent OTP reuse
    @Column(name = "is_used", nullable = false)
    private boolean isUsed = false;

    // Track wrong OTP attempts
    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts = 0;

    // Lock this OTP after too many wrong attempts
    @Column(name = "is_locked", nullable = false)
    private boolean isLocked = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
