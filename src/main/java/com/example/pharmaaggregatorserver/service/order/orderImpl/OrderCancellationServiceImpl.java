package com.example.pharmaaggregatorserver.service.order.orderImpl;

import com.example.pharmaaggregatorserver.dto.order.OrderResponseDTO;
import com.example.pharmaaggregatorserver.dto.order.SellerOrderResponseDTO;
import com.example.pharmaaggregatorserver.dto.product.StockInRequestDto;
import com.example.pharmaaggregatorserver.entity.order.Order;
import com.example.pharmaaggregatorserver.entity.order.OrderItem;
import com.example.pharmaaggregatorserver.entity.order.OrderStatusHistory;
import com.example.pharmaaggregatorserver.entity.order.PaymentStatus;
import com.example.pharmaaggregatorserver.entity.order.Refund;
import com.example.pharmaaggregatorserver.entity.order.RefundStatus;
import com.example.pharmaaggregatorserver.entity.order.SellerOrder;
import com.example.pharmaaggregatorserver.entity.order.SellerOrderStatus;
import com.example.pharmaaggregatorserver.exception.BadRequestException;
import com.example.pharmaaggregatorserver.exception.ResourceNotFoundException;
import com.example.pharmaaggregatorserver.exception.UnauthorizedException;
import com.example.pharmaaggregatorserver.repository.order.OrderRepository;
import com.example.pharmaaggregatorserver.repository.order.RefundRepository;
import com.example.pharmaaggregatorserver.repository.order.SellerOrderRepository;
import com.example.pharmaaggregatorserver.service.order.OrderCancellationService;
import com.example.pharmaaggregatorserver.service.order.support.OrderMapper;
import com.example.pharmaaggregatorserver.service.order.support.OrderStatusRollup;
import com.example.pharmaaggregatorserver.service.product.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderCancellationServiceImpl implements OrderCancellationService {

    private static final Set<String> CANCELLABLE_STATUSES = Set.of(
            SellerOrderStatus.PLACED, SellerOrderStatus.CONFIRMED, SellerOrderStatus.PACKED);

    private final OrderRepository orderRepository;
    private final SellerOrderRepository sellerOrderRepository;
    private final RefundRepository refundRepository;
    private final StockService stockService;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderResponseDTO cancelOrder(String orderId, String actorRole, String actorId, String reason) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        validateWholeOrderCancelActor(order, actorRole, actorId);

        boolean anyCancelled = false;
        for (SellerOrder sellerOrder : order.getSellerOrders()) {
            if (CANCELLABLE_STATUSES.contains(sellerOrder.getStatus())) {
                cancelSellerOrderInternal(sellerOrder, actorRole, actorId, reason);
                anyCancelled = true;
            }
        }

        if (!anyCancelled) {
            throw new BadRequestException(
                    "Order " + orderId + " has no seller orders eligible for cancellation");
        }

        recomputeOrderRollup(order);
        order = orderRepository.save(order);
        return orderMapper.toOrderDto(order);
    }

    @Override
    @Transactional
    public SellerOrderResponseDTO cancelSellerOrder(String sellerOrderId, String actorRole, String actorId, String reason) {
        SellerOrder sellerOrder = sellerOrderRepository.findBySellerOrderId(sellerOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller order not found: " + sellerOrderId));

        validateSellerOrderCancelActor(sellerOrder, actorRole, actorId);

        if (!CANCELLABLE_STATUSES.contains(sellerOrder.getStatus())) {
            throw new BadRequestException(
                    "Seller order " + sellerOrderId + " cannot be cancelled from status " + sellerOrder.getStatus());
        }

        cancelSellerOrderInternal(sellerOrder, actorRole, actorId, reason);

        Order order = sellerOrder.getOrder();
        recomputeOrderRollup(order);
        orderRepository.save(order);

        return orderMapper.toSellerOrderDto(sellerOrder);
    }

    // No Buyer/Seller security principal exists yet (see PlaceOrderRequestDTO's own
    // comment), so actorRole/actorId are caller-supplied — but that means without an
    // explicit ownership check, ANY buyer could cancel ANY other buyer's order just
    // by passing a different actorId. This was found live (buyer OMHOS0001 was able
    // to cancel buyer EDHOS0001's order) and is fixed here, plus in the seller-order
    // variant below.
    private void validateWholeOrderCancelActor(Order order, String actorRole, String actorId) {
        if ("BUYER".equalsIgnoreCase(actorRole)) {
            if (!order.getBuyer().getBuyerId().equals(actorId)) {
                throw new UnauthorizedException("Order " + order.getOrderId() + " does not belong to buyer " + actorId);
            }
        } else if ("SELLER".equalsIgnoreCase(actorRole)) {
            // A whole-Order cancel can span multiple sellers' sub-orders — a seller
            // only ever owns one of them, so this endpoint isn't the right one for a
            // seller-initiated cancel. Direct them to cancelSellerOrder instead.
            throw new UnauthorizedException(
                    "Sellers cannot cancel a whole Order — cancel your own SellerOrder via /seller-orders/{id}/cancel instead");
        }
        // ADMIN is unrestricted by design (the escalation path, mirroring how admin
        // review already works elsewhere in this codebase).
    }

    private void validateSellerOrderCancelActor(SellerOrder sellerOrder, String actorRole, String actorId) {
        if ("SELLER".equalsIgnoreCase(actorRole)) {
            if (!sellerOrder.getSeller().getSellerId().equals(actorId)) {
                throw new UnauthorizedException(
                        "Seller order " + sellerOrder.getSellerOrderId() + " does not belong to seller " + actorId);
            }
        } else if ("BUYER".equalsIgnoreCase(actorRole)) {
            if (!sellerOrder.getOrder().getBuyer().getBuyerId().equals(actorId)) {
                throw new UnauthorizedException(
                        "Seller order " + sellerOrder.getSellerOrderId() + " does not belong to buyer " + actorId);
            }
        }
        // ADMIN is unrestricted by design.
    }

    private void cancelSellerOrderInternal(SellerOrder sellerOrder, String actorRole, String actorId, String reason) {
        String fromStatus = sellerOrder.getStatus();

        // Reverse the stock debit taken at placement, one restock call per item,
        // via the existing addStock method (restocks the batch by matching
        // batchLotNumber on the same product — no new stock-mutation logic).
        for (OrderItem item : sellerOrder.getOrderItems()) {
            restockItem(item, "ORDER_CANCEL", sellerOrder);
            item.setItemStatus(SellerOrderStatus.CANCELLED);
        }

        sellerOrder.setStatus(SellerOrderStatus.CANCELLED);
        sellerOrder.setCancelledAt(LocalDateTime.now());
        sellerOrder.setCancelReason(reason);
        sellerOrder.setCancelledByRole(actorRole);

        OrderStatusHistory history = new OrderStatusHistory();
        history.setSellerOrder(sellerOrder);
        history.setFromStatus(fromStatus);
        history.setToStatus(SellerOrderStatus.CANCELLED);
        history.setChangedByRole(actorRole);
        history.setChangedById(actorId);
        history.setComment(reason);
        sellerOrder.getStatusHistory().add(history);

        Order order = sellerOrder.getOrder();
        if (order.getPayment() != null && PaymentStatus.SUCCESS.equals(order.getPayment().getStatus())) {
            Refund refund = new Refund();
            refund.setPayment(order.getPayment());
            refund.setOrderItem(null); // whole seller-order refund
            refund.setAmount(sellerOrder.getGrandTotal());
            refund.setStatus(RefundStatus.REQUESTED);
            refund.setReason("Seller order " + sellerOrder.getSellerOrderId() + " cancelled: " + reason);
            refund.setRequestedByRole(actorRole);
            refundRepository.save(refund);
        }

        sellerOrderRepository.save(sellerOrder);
    }

    private void restockItem(OrderItem item, String referenceType, SellerOrder sellerOrder) {
        StockInRequestDto restock = new StockInRequestDto();
        restock.setProductId(item.getProductDetails().getProductId());
        restock.setPackagingId(item.getPackagingIdSnapshot());
        restock.setBatchLotNumber(item.getBatchLotNumberSnapshot());
        restock.setQuantity(item.getQuantity().longValue());
        restock.setReferenceId(sellerOrder.getSellerOrderId());
        restock.setReferenceType(referenceType);
        // resolveOrCreateBatch matches an existing batch by (productId, batchLotNumber)
        // and then requires expiryDate to be equal — leaving it null here always fails
        // that check against the real batch's expiry date, so the restock must carry
        // the exact same manufacturing/expiry dates as the batch actually debited.
        if (item.getPricingDetails() != null) {
            restock.setManufacturingDate(item.getPricingDetails().getManufacturingDate());
            restock.setExpiryDate(item.getPricingDetails().getExpiryDate());
        }

        Long sellerUserId = sellerOrder.getSeller().getUser() != null
                ? sellerOrder.getSeller().getUser().getUserId() : null;
        stockService.addStock(restock, sellerUserId);
    }

    /**
     * Reusable rollup call — recomputes and applies the parent Order's status
     * from its current children, per {@link OrderStatusRollup}. Callers still
     * need to save the Order afterward.
     */
    private void recomputeOrderRollup(Order order) {
        List<String> childStatuses = order.getSellerOrders().stream().map(SellerOrder::getStatus).toList();
        order.setStatus(OrderStatusRollup.compute(childStatuses));
        if (SellerOrderStatus.CANCELLED.equals(OrderStatusRollup.compute(childStatuses))
                && order.getCancelledAt() == null) {
            order.setCancelledAt(LocalDateTime.now());
        }
    }
}
