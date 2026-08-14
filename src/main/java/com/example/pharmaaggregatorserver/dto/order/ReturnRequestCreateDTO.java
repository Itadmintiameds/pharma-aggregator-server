package com.example.pharmaaggregatorserver.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReturnRequestCreateDTO {

    @NotNull(message = "orderItemId is required")
    private Long orderItemId;

    @NotBlank(message = "buyerId is required")
    private String buyerId;

    @NotBlank(message = "reason is required")
    private String reason;
}
