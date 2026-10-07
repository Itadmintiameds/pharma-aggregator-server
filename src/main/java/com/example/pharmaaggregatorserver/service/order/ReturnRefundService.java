package com.example.pharmaaggregatorserver.service.order;

import com.example.pharmaaggregatorserver.dto.order.ReturnResponseDTO;

public interface ReturnRefundService {

    ReturnResponseDTO requestReturn(Long orderItemId, String buyerId, String reason);

    ReturnResponseDTO decideReturn(Long returnId, String sellerId, boolean approve, String comment);

    ReturnResponseDTO processRefund(Long refundId);

    ReturnResponseDTO getReturn(Long returnId);
}
