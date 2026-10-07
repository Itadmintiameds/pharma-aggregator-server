package com.example.pharmaaggregatorserver.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReturnDecisionRequestDTO {

    @NotBlank(message = "sellerId is required")
    private String sellerId;

    @NotNull(message = "approve is required")
    private Boolean approve;

    private String comment;
}
