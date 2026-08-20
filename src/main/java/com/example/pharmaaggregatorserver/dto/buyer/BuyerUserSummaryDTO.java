package com.example.pharmaaggregatorserver.dto.buyer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Lightweight "who am I" payload for GET /buyer/authentication/me — lets the
// frontend refresh the logged-in buyer's email/phone on demand instead of
// only trusting whatever was cached in localStorage at the last login.
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BuyerUserSummaryDTO {
    private Long buyerUserId;
    private String email;
    private String phone;
}
