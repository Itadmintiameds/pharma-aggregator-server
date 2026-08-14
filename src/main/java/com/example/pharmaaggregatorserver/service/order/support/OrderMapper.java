package com.example.pharmaaggregatorserver.service.order.support;

import com.example.pharmaaggregatorserver.dto.order.OrderItemResponseDTO;
import com.example.pharmaaggregatorserver.dto.order.OrderResponseDTO;
import com.example.pharmaaggregatorserver.dto.order.OrderStatusHistoryResponseDTO;
import com.example.pharmaaggregatorserver.dto.order.SellerOrderResponseDTO;
import com.example.pharmaaggregatorserver.entity.order.Order;
import com.example.pharmaaggregatorserver.entity.order.OrderItem;
import com.example.pharmaaggregatorserver.entity.order.OrderStatusHistory;
import com.example.pharmaaggregatorserver.entity.order.Payment;
import com.example.pharmaaggregatorserver.entity.order.SellerOrder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Builds flat response DTOs from order entities. Kept as the single place
 * that walks Order/SellerOrder/OrderItem relations for serialization so no
 * controller or service accidentally returns a raw entity (which would risk
 * chaining into Buyer -> BuyerUser.passwordHash or Seller -> User.passwordHash).
 */
@Component
public class OrderMapper {

    public OrderItemResponseDTO toItemDto(OrderItem item) {
        return OrderItemResponseDTO.builder()
                .orderItemId(item.getOrderItemId())
                .productId(item.getProductDetails() != null ? item.getProductDetails().getProductId() : null)
                .pricingId(item.getPricingDetails() != null ? item.getPricingDetails().getPricingId() : null)
                .productNameSnapshot(item.getProductNameSnapshot())
                .batchLotNumberSnapshot(item.getBatchLotNumberSnapshot())
                .packagingIdSnapshot(item.getPackagingIdSnapshot())
                .quantity(item.getQuantity())
                .unitPriceSnapshot(item.getUnitPriceSnapshot())
                .discountAmount(item.getDiscountAmount())
                .taxAmount(item.getTaxAmount())
                .lineTotal(item.getLineTotal())
                .itemStatus(item.getItemStatus())
                .build();
    }

    public OrderStatusHistoryResponseDTO toHistoryDto(OrderStatusHistory h) {
        return OrderStatusHistoryResponseDTO.builder()
                .historyId(h.getHistoryId())
                .fromStatus(h.getFromStatus())
                .toStatus(h.getToStatus())
                .changedByRole(h.getChangedByRole())
                .changedById(h.getChangedById())
                .comment(h.getComment())
                .changedAt(h.getChangedAt())
                .build();
    }

    public SellerOrderResponseDTO toSellerOrderDto(SellerOrder so) {
        List<OrderItemResponseDTO> items = so.getOrderItems() == null ? List.of()
                : so.getOrderItems().stream().map(this::toItemDto).toList();
        List<OrderStatusHistoryResponseDTO> history = so.getStatusHistory() == null ? List.of()
                : so.getStatusHistory().stream().map(this::toHistoryDto).toList();

        return SellerOrderResponseDTO.builder()
                .sellerOrderId(so.getSellerOrderId())
                .orderId(so.getOrder() != null ? so.getOrder().getOrderId() : null)
                .sellerId(so.getSeller() != null ? so.getSeller().getSellerId() : null)
                .status(so.getStatus())
                .subtotal(so.getSubtotal())
                .shippingFee(so.getShippingFee())
                .taxAmount(so.getTaxAmount())
                .grandTotal(so.getGrandTotal())
                .courierName(so.getCourierName())
                .trackingNumber(so.getTrackingNumber())
                .trackingUrl(so.getTrackingUrl())
                .confirmedAt(so.getConfirmedAt())
                .shippedAt(so.getShippedAt())
                .deliveredAt(so.getDeliveredAt())
                .cancelledAt(so.getCancelledAt())
                .cancelReason(so.getCancelReason())
                .cancelledByRole(so.getCancelledByRole())
                .invoiceId(so.getInvoice() != null ? so.getInvoice().getInvoiceId() : null)
                .items(items)
                .statusHistory(history)
                .createdAt(so.getCreatedAt())
                .build();
    }

    public OrderResponseDTO toOrderDto(Order order) {
        Payment payment = order.getPayment();
        List<SellerOrderResponseDTO> sellerOrders = order.getSellerOrders() == null ? List.of()
                : order.getSellerOrders().stream().map(this::toSellerOrderDto).toList();

        return OrderResponseDTO.builder()
                .orderId(order.getOrderId())
                .buyerId(order.getBuyer() != null ? order.getBuyer().getBuyerId() : null)
                .deliveryName(order.getDeliveryName())
                .deliveryPhone(order.getDeliveryPhone())
                .deliveryAddressLine(order.getDeliveryAddressLine())
                .deliveryCity(order.getDeliveryCity())
                .deliveryDistrict(order.getDeliveryDistrict())
                .deliveryState(order.getDeliveryState())
                .deliveryPinCode(order.getDeliveryPinCode())
                .status(order.getStatus())
                .itemCount(order.getItemCount())
                .sellerOrderCount(order.getSellerOrderCount())
                .subtotal(order.getSubtotal())
                .shippingTotal(order.getShippingTotal())
                .taxTotal(order.getTaxTotal())
                .grandTotal(order.getGrandTotal())
                .paymentId(payment != null ? payment.getPaymentId() : null)
                .paymentStatus(payment != null ? payment.getStatus() : null)
                .placedAt(order.getPlacedAt())
                .cancelledAt(order.getCancelledAt())
                .cancelledByRole(order.getCancelledByRole())
                .cancelReason(order.getCancelReason())
                .sellerOrders(sellerOrders)
                .createdAt(order.getCreatedAt())
                .build();
    }
}
