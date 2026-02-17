package com.example.pharmaaggregatorserver.dto.seller.OnSubmit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailStatusDTO {
    private String applicationRequestId;
    private String coordinatorEmail;
    private boolean emailSent;
    private LocalDateTime emailSentAt;
    private String emailStatus;
    private String errorMessage;
}