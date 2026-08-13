package com.example.pharmaaggregatorserver.dto.buyer;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BuyerResponseDTO {
    private String buyerId;
    private Long tempBuyerId;
    private String organizationName;
    private String orgLogoUrl;
    private String status;
    private LocalDateTime approvedAt;
}
