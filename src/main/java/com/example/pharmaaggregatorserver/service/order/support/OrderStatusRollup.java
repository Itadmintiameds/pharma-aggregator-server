package com.example.pharmaaggregatorserver.service.order.support;

import com.example.pharmaaggregatorserver.entity.order.OrderStatus;
import com.example.pharmaaggregatorserver.entity.order.SellerOrderStatus;

import java.util.List;
import java.util.Set;

/**
 * Pure function that derives a parent {@code Order}'s rollup status from the
 * statuses of its child {@code SellerOrder} rows. Reused by every service
 * that changes a SellerOrder's status (cancellation, fulfilment, returns) so
 * the parent Order.status is always recomputed the same way.
 * <p>
 * Rules:
 * - All children CANCELLED -> CANCELLED.
 * - All non-cancelled children DELIVERED (or a post-delivery return/refund
 * state, which only happens after delivery) -> DELIVERED.
 * - All non-cancelled children at SHIPPED-or-beyond -> SHIPPED.
 * - Some (but not all) non-cancelled children at SHIPPED-or-beyond -> PARTIALLY_SHIPPED.
 * - Otherwise -> PLACED.
 */
public final class OrderStatusRollup {

    private static final Set<String> POST_DELIVERY_STATES = Set.of(
            SellerOrderStatus.RETURN_REQUESTED,
            SellerOrderStatus.RETURN_APPROVED,
            SellerOrderStatus.RETURN_REJECTED,
            SellerOrderStatus.RETURNED,
            SellerOrderStatus.REFUNDED
    );

    private static final Set<String> SHIPPED_OR_BEYOND = Set.of(
            SellerOrderStatus.SHIPPED,
            SellerOrderStatus.OUT_FOR_DELIVERY,
            SellerOrderStatus.DELIVERED
    );

    private OrderStatusRollup() {
    }

    public static String compute(List<String> childStatuses) {
        if (childStatuses == null || childStatuses.isEmpty()) {
            return OrderStatus.PLACED;
        }

        List<String> active = childStatuses.stream()
                .filter(s -> !SellerOrderStatus.CANCELLED.equals(s))
                .toList();

        if (active.isEmpty()) {
            // every child was cancelled
            return OrderStatus.CANCELLED;
        }

        boolean allDelivered = active.stream().allMatch(OrderStatusRollup::isDeliveredOrLater);
        if (allDelivered) {
            return OrderStatus.DELIVERED;
        }

        boolean allShippedOrBeyond = active.stream().allMatch(OrderStatusRollup::isShippedOrBeyond);
        if (allShippedOrBeyond) {
            return OrderStatus.SHIPPED;
        }

        boolean anyShippedOrBeyond = active.stream().anyMatch(OrderStatusRollup::isShippedOrBeyond);
        if (anyShippedOrBeyond) {
            return OrderStatus.PARTIALLY_SHIPPED;
        }

        return OrderStatus.PLACED;
    }

    private static boolean isShippedOrBeyond(String status) {
        return SHIPPED_OR_BEYOND.contains(status) || isDeliveredOrLater(status);
    }

    private static boolean isDeliveredOrLater(String status) {
        return SellerOrderStatus.DELIVERED.equals(status) || POST_DELIVERY_STATES.contains(status);
    }
}
