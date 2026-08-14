package com.example.pharmaaggregatorserver.service.order;

import com.example.pharmaaggregatorserver.dto.order.PaymentResponseDTO;

/**
 * No payment gateway/webhook is integrated in this build — Payment rows are
 * created COD-and-SUCCESS at order placement (see
 * OrderPlacementServiceImpl). This service is read-only.
 */
public interface PaymentService {

    PaymentResponseDTO getByPaymentId(String paymentId);
}
