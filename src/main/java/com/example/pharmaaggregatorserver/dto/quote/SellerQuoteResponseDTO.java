package com.example.pharmaaggregatorserver.dto.quote;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SellerQuoteResponseDTO {

    @NotNull(message = "Quoted price is required")
    @Positive(message = "Quoted price must be greater than zero")
    private BigDecimal quotedPrice;

    private LocalDate quoteValidUntil;

    private String sellerNotes;
}
