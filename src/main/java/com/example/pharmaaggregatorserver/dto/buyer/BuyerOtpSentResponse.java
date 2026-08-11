package com.example.pharmaaggregatorserver.dto.buyer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BuyerOtpSentResponse {
    private String message;   // "OTP sent to registered email"
    private String username;  // echo back so the frontend can use it in Step 2
}
