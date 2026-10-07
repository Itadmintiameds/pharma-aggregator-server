package com.example.pharmaaggregatorserver.dto.quote;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class QuoteRequestCreateDTO {

    @NotBlank(message = "Product is required")
    private String productId;

    @NotBlank(message = "Request type is required")
    private String requestType; // "PRICE_REQUEST" or "RFQ"

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than zero")
    private Integer quantity;

    private String unit;

    // PRICE_REQUEST-only fields
    private BigDecimal targetPrice;
    private String pincode;

    // RFQ-only fields
    private String deliveryLocation;
    private LocalDate expectedDeliveryDate;
    private String paymentTerms;
    private String companyName;
    private String gstNumber;

    // Doubles as the universal "requester name" for both request types (the
    // buyer's own full name for PRICE_REQUEST, the business contact for RFQ)
    // — used to name the auto-provisioned guest account when the caller
    // isn't logged in.
    @NotBlank(message = "Contact name is required")
    private String contactPerson;

    @NotBlank(message = "Phone number is required")
    private String phone;

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email address")
    private String email;

    private String message;
}
