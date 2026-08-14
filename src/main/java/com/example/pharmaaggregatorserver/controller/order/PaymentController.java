package com.example.pharmaaggregatorserver.controller.order;

import com.example.pharmaaggregatorserver.dto.order.PaymentResponseDTO;
import com.example.pharmaaggregatorserver.response.ApiResponse;
import com.example.pharmaaggregatorserver.service.order.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * No payment gateway/webhook is integrated in this build (COD-only) — this
 * controller is read-only, just exposing the Payment record created at
 * order placement.
 */
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentResponseDTO>> getPayment(@PathVariable String paymentId) {
        PaymentResponseDTO response = paymentService.getByPaymentId(paymentId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.toString(), "Payment fetched successfully", response));
    }
}
