package com.example.pharmaaggregatorserver.dto.order;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShipSellerOrderRequestDTO {

    @NotBlank(message = "sellerId is required")
    private String sellerId;

    private String courierName;
    private String trackingNumber;
    private String trackingUrl;
}
