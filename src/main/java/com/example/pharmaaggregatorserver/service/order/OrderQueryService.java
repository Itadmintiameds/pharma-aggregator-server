package com.example.pharmaaggregatorserver.service.order;

import com.example.pharmaaggregatorserver.dto.order.OrderResponseDTO;
import com.example.pharmaaggregatorserver.dto.order.SellerOrderResponseDTO;

import java.util.List;

/**
 * Read-side queries for orders/seller-orders, plus the admin force-override
 * action — kept separate from OrderPlacementService/OrderCancellationService/
 * SellerOrderFulfillmentService, which are all write-side.
 */
public interface OrderQueryService {

    OrderResponseDTO getOrder(String orderId);

    List<OrderResponseDTO> getOrdersByBuyer(String buyerId);

    List<OrderResponseDTO> getAllOrders(String status);

    SellerOrderResponseDTO getSellerOrder(String sellerOrderId);

    List<SellerOrderResponseDTO> getSellerOrdersBySeller(String sellerId, String status);

    /**
     * Admin-only force transition: sets every SellerOrder under the given
     * Order to newStatus, bypassing SellerOrderFulfillmentServiceImpl's legal
     * transition check, still writing an OrderStatusHistory row per
     * SellerOrder with changedByRole=ADMIN.
     */
    OrderResponseDTO adminOverride(String orderId, String newStatus, String reason, String adminId);
}
