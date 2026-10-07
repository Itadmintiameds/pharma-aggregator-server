package com.example.pharmaaggregatorserver.dto.product;

import lombok.Data;

import java.util.UUID;

@Data
public class ProductUserManualDto {

    private UUID userManualId;
    private String userManualUrl;

}
