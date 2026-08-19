package com.example.pharmaaggregatorserver.dto.buyer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BuyerLoginResponse {
    private String accessToken;
    private String refreshToken;   // raw token, sent once
    private String type = "Bearer";
    private Long buyerUserId;
    private String username;
    private Set<String> roles;
    private boolean passwordTemporary;
    private String message;
}
