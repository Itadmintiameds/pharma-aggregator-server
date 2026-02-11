package com.example.pharmaaggregatorserver.dto.seller;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OtpResponseDTO {

    private String status;
    private String message;
}