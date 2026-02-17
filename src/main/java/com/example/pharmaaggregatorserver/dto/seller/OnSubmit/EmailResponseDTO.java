package com.example.pharmaaggregatorserver.dto.seller.OnSubmit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailResponseDTO {
    private boolean success;
    private String message;
    private String applicationRequestId;
    private String submissionDate;
    private String coordinatorEmail;
    private String coordinatorName;
    private LocalDateTime timestamp;
}