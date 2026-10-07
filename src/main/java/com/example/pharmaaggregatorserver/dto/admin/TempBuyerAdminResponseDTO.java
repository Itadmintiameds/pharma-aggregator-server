package com.example.pharmaaggregatorserver.dto.admin;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TempBuyerAdminResponseDTO {

    private Long tempBuyerId;
    private String tempBuyerRequestId;
    private String organizationName;
    private String contactEmail;
    private LocalDateTime createdAt;
    private String status;
}
