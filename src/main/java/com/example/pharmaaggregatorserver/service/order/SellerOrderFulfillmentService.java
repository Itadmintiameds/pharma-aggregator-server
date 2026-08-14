package com.example.pharmaaggregatorserver.service.order;

import com.example.pharmaaggregatorserver.dto.order.SellerOrderResponseDTO;

public interface SellerOrderFulfillmentService {

    SellerOrderResponseDTO confirm(String sellerOrderId, String sellerId);

    SellerOrderResponseDTO pack(String sellerOrderId, String sellerId);

    SellerOrderResponseDTO ship(String sellerOrderId, String sellerId, String courierName, String trackingNumber, String trackingUrl);

    SellerOrderResponseDTO markOutForDelivery(String sellerOrderId, String sellerId);

    SellerOrderResponseDTO markDelivered(String sellerOrderId, String sellerId);
}
