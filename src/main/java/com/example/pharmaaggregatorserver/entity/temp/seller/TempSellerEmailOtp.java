package com.example.pharmaaggregatorserver.entity.temp.seller;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tbl_temp_seller_email_otp")
public class TempSellerEmailOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long coordinatorId;

    private String email;

    private String otp;

    private LocalDateTime expiryTime;

    private boolean verified;
}
