package com.example.pharmaaggregatorserver.dto.seller.SellerLogIn;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String accessToken;    // was: token
    private String refreshToken;   // new — raw token, sent once
    private String type = "Bearer";
    private Long userId;
    private String username;
    private Set<String> roles;
    private boolean passwordTemporary;
    private String message;
}