package com.example.pharmaaggregatorserver.entity.buyer;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tbl_buyer_signup_otp")
public class BuyerSignupOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    @Column(name = "full_name")
    private String fullName;

    private String phone;

    private String otp;

    // BCrypt hash of the password chosen at signup — the BuyerUser row is
    // only created once this OTP is verified, so the hash is held here until then.
    @Column(name = "password_hash")
    private String passwordHash;

    private LocalDateTime expiryTime;

    private boolean verified;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
