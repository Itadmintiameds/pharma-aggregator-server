package com.example.pharmaaggregatorserver.dto.admin;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TempSellerAdminResponseDTO {

    private Long tempSellerId;
    private String tempSellerRequestId;
    private String tempSellerName;
    private String tempSellerEmail;
    private LocalDateTime createdAt;
    private String status;
}