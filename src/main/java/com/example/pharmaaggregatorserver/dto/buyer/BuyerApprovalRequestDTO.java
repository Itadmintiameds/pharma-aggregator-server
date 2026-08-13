package com.example.pharmaaggregatorserver.dto.buyer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BuyerApprovalRequestDTO {

    @NotNull(message = "Buyer ID is required")
    private Long id;

    @NotBlank(message = "Status cannot be empty")
    private String status;     // ACCEPT, REJECT, CORRECTION

    @NotBlank(message = "Comment cannot be empty")
    private String comments;
}
