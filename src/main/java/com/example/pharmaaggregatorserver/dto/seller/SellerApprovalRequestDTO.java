package com.example.pharmaaggregatorserver.dto.seller;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SellerApprovalRequestDTO {

    @NotNull(message = "Seller ID is required")
    private Long id;

    @NotNull(message = "Status is mandatory")
    private String status;     // ACCEPT, REJECT, CORRECTION
    private String comments;
}