package com.example.pharmaaggregatorserver.service.temp.seller.OnSubmit;

import com.example.pharmaaggregatorserver.dto.seller.OnSubmit.EmailRequestDTO;
import com.example.pharmaaggregatorserver.dto.seller.OnSubmit.EmailResponseDTO;
import com.example.pharmaaggregatorserver.dto.seller.OnSubmit.EmailStatusDTO;

public interface IndependentEmailService {
    EmailResponseDTO sendApplicationConfirmationEmail(EmailRequestDTO request);
    EmailResponseDTO sendCustomEmail(String to, String subject, String body);
    EmailStatusDTO checkEmailStatus(String applicationRequestId);
}