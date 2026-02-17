package com.example.pharmaaggregatorserver.controller.temp.seller.OnSubmit;

import com.example.pharmaaggregatorserver.dto.seller.OnSubmit.EmailRequestDTO;
import com.example.pharmaaggregatorserver.dto.seller.OnSubmit.EmailResponseDTO;
import com.example.pharmaaggregatorserver.dto.seller.OnSubmit.EmailStatusDTO;
import com.example.pharmaaggregatorserver.service.temp.seller.OnSubmit.IndependentEmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/independent/email")
@RequiredArgsConstructor
@Tag(name = "📧 Independent Email API", description = "Completely standalone email endpoints - NO DEPENDENCIES on TempSeller")
public class IndependentEmailController {

    private final IndependentEmailService independentEmailService;

    @PostMapping("/send-confirmation")
    @Operation(summary = "Send application confirmation email using provided data")
    public ResponseEntity<EmailResponseDTO> sendConfirmationEmail(@RequestBody EmailRequestDTO request) {
        EmailResponseDTO response = independentEmailService.sendApplicationConfirmationEmail(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/send")
    @Operation(summary = "Send custom email")
    public ResponseEntity<EmailResponseDTO> sendCustomEmail(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam String body) {
        EmailResponseDTO response = independentEmailService.sendCustomEmail(to, subject, body);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{applicationRequestId}")
    @Operation(summary = "Check email status by Application Request ID")
    public ResponseEntity<EmailStatusDTO> getEmailStatus(@PathVariable String applicationRequestId) {
        EmailStatusDTO status = independentEmailService.checkEmailStatus(applicationRequestId);
        return ResponseEntity.ok(status);
    }

    @PostMapping("/send-quick")
    @Operation(summary = "Quick send with minimal parameters")
    public ResponseEntity<EmailResponseDTO> sendQuickEmail(
            @RequestParam String coordinatorEmail,
            @RequestParam String coordinatorName,
            @RequestParam String applicationRequestId,
            @RequestParam String sellerName) {

        EmailRequestDTO request = new EmailRequestDTO();
        request.setCoordinatorEmail(coordinatorEmail);
        request.setCoordinatorName(coordinatorName);
        request.setApplicationRequestId(applicationRequestId);
        request.setSellerName(sellerName);
        request.setCoordinatorDesignation("Coordinator");

        EmailResponseDTO response = independentEmailService.sendApplicationConfirmationEmail(request);
        return ResponseEntity.ok(response);
    }
}
