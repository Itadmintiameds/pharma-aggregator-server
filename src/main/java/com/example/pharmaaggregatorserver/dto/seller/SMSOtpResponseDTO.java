package com.example.pharmaaggregatorserver.dto.seller;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SMSOtpResponseDTO {
    private String status;
    private String message;
}