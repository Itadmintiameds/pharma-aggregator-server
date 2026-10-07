package com.example.pharmaaggregatorserver.service.order;

import com.example.pharmaaggregatorserver.dto.order.OrderResponseDTO;
import com.example.pharmaaggregatorserver.dto.order.SellerOrderResponseDTO;

public interface OrderCancellationService {

    OrderResponseDTO cancelOrder(String orderId, String actorRole, String actorId, String reason);

    SellerOrderResponseDTO cancelSellerOrder(String sellerOrderId, String actorRole, String actorId, String reason);
}
