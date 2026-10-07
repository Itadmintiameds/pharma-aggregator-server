package com.example.pharmaaggregatorserver.service.order.orderImpl;

import com.example.pharmaaggregatorserver.dto.order.PaymentResponseDTO;
import com.example.pharmaaggregatorserver.entity.order.Payment;
import com.example.pharmaaggregatorserver.exception.ResourceNotFoundException;
import com.example.pharmaaggregatorserver.repository.order.PaymentRepository;
import com.example.pharmaaggregatorserver.service.order.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * No payment gateway/webhook is integrated in this build — Payment rows are
 * created COD-and-SUCCESS at order placement (see
 * OrderPlacementServiceImpl#placeOrder). This service is a read-only lookup.
 */
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    @Override
    public PaymentResponseDTO getByPaymentId(String paymentId) {
        Payment payment = paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + paymentId));
        return toDto(payment);
    }

    private PaymentResponseDTO toDto(Payment payment) {
        return PaymentResponseDTO.builder()
                .paymentId(payment.getPaymentId())
                .orderId(payment.getOrder() != null ? payment.getOrder().getOrderId() : null)
                .provider(payment.getProvider())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .providerOrderId(payment.getProviderOrderId())
                .providerTransactionId(payment.getProviderTransactionId())
                .paidAt(payment.getPaidAt())
                .failureReason(payment.getFailureReason())
                .build();
    }
}
