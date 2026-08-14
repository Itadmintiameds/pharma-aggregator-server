package com.example.pharmaaggregatorserver.service.order;

import com.example.pharmaaggregatorserver.dto.order.OrderResponseDTO;
import com.example.pharmaaggregatorserver.dto.order.PlaceOrderRequestDTO;

public interface OrderPlacementService {

    OrderResponseDTO placeOrder(PlaceOrderRequestDTO request);
}
