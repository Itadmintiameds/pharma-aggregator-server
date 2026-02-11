package com.example.pharmaaggregatorserver.entity.temp.seller;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "phone_otp")
public class PhoneOTP {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String phone;
    private String otp;
    private LocalDateTime expiryTime;
    private boolean verified;
}