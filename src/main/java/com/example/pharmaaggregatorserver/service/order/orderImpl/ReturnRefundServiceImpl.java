package com.example.pharmaaggregatorserver.service.order.orderImpl;

import com.example.pharmaaggregatorserver.dto.order.ReturnResponseDTO;
import com.example.pharmaaggregatorserver.dto.product.StockInRequestDto;
import com.example.pharmaaggregatorserver.entity.buyer.Buyer;
import com.example.pharmaaggregatorserver.entity.order.Order;
import com.example.pharmaaggregatorserver.entity.order.OrderItem;
import com.example.pharmaaggregatorserver.entity.order.OrderStatusHistory;
import com.example.pharmaaggregatorserver.entity.order.Payment;
import com.example.pharmaaggregatorserver.entity.order.Refund;
import com.example.pharmaaggregatorserver.entity.order.RefundStatus;
import com.example.pharmaaggregatorserver.entity.order.ReturnRequest;
import com.example.pharmaaggregatorserver.entity.order.ReturnStatus;
import com.example.pharmaaggregatorserver.entity.order.SellerOrder;
import com.example.pharmaaggregatorserver.entity.order.SellerOrderStatus;
import com.example.pharmaaggregatorserver.exception.BadRequestException;
import com.example.pharmaaggregatorserver.exception.ResourceNotFoundException;
import com.example.pharmaaggregatorserver.exception.UnauthorizedException;
import com.example.pharmaaggregatorserver.repository.buyer.BuyerRepository;
import com.example.pharmaaggregatorserver.repository.order.OrderItemRepository;
import com.example.pharmaaggregatorserver.repository.order.OrderRepository;
import com.example.pharmaaggregatorserver.repository.order.RefundRepository;
import com.example.pharmaaggregatorserver.repository.order.ReturnRequestRepository;
import com.example.pharmaaggregatorserver.repository.order.SellerOrderRepository;
import com.example.pharmaaggregatorserver.service.order.ReturnRefundService;
import com.example.pharmaaggregatorserver.service.order.support.OrderStatusRollup;
import com.example.pharmaaggregatorserver.service.product.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Deviation: {@code ReturnRequest} is modelled per-{@code OrderItem} (per the
 * spec's entity design), but the given {@code SellerOrderStatus} list only
 * has whole-seller-order RETURN_* states (no per-item variant). This
 * implementation therefore also pushes the parent SellerOrder through
 * RETURN_REQUESTED / RETURN_APPROVED / RETURN_REJECTED / RETURNED alongside
 * the per-item ReturnRequest row — acceptable for a single-item-per-seller-order
 * return, but note it does not model two concurrent partial-item returns
 * against the same SellerOrder distinctly at the SellerOrder-status level.
 */
@Service
@RequiredArgsConstructor
public class ReturnRefundServiceImpl implements ReturnRefundService {

    // Placeholder pending a real business decision on the return policy window.
    private static final int RETURN_WINDOW_DAYS = 7;

    private final OrderItemRepository orderItemRepository;
    private final BuyerRepository buyerRepository;
    private final ReturnRequestRepository returnRequestRepository;
    private final RefundRepository refundRepository;
    private final SellerOrderRepository sellerOrderRepository;
    private final OrderRepository orderRepository;
    private final StockService stockService;

    @Override
    @Transactional
    public ReturnResponseDTO requestReturn(Long orderItemId, String buyerId, String reason) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found: " + orderItemId));

        SellerOrder sellerOrder = orderItem.getSellerOrder();
        Order order = sellerOrder.getOrder();

        if (!order.getBuyer().getBuyerId().equals(buyerId)) {
            throw new UnauthorizedException("Order item " + orderItemId + " does not belong to buyer " + buyerId);
        }

        if (!SellerOrderStatus.DELIVERED.equals(sellerOrder.getStatus())) {
            throw new BadRequestException(
                    "Order item " + orderItemId + " cannot be returned — seller order is not DELIVERED");
        }

        if (sellerOrder.getDeliveredAt() == null
                || LocalDateTime.now().isAfter(sellerOrder.getDeliveredAt().plusDays(RETURN_WINDOW_DAYS))) {
            throw new BadRequestException(
                    "Return window of " + RETURN_WINDOW_DAYS + " days has expired for order item " + orderItemId);
        }

        Buyer buyer = buyerRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer not found: " + buyerId));

        ReturnRequest returnRequest = new ReturnRequest();
        returnRequest.setOrderItem(orderItem);
        returnRequest.setBuyer(buyer);
        returnRequest.setReason(reason);
        returnRequest.setStatus(ReturnStatus.REQUESTED);
        returnRequest = returnRequestRepository.save(returnRequest);

        transitionSellerOrder(sellerOrder, SellerOrderStatus.RETURN_REQUESTED, "BUYER", buyerId, reason);

        return toDto(returnRequest);
    }

    @Override
    @Transactional
    public ReturnResponseDTO decideReturn(Long returnId, String sellerId, boolean approve, String comment) {
        ReturnRequest returnRequest = returnRequestRepository.findById(returnId)
                .orElseThrow(() -> new ResourceNotFoundException("Return request not found: " + returnId));

        SellerOrder sellerOrder = returnRequest.getOrderItem().getSellerOrder();
        if (!sellerOrder.getSeller().getSellerId().equals(sellerId)) {
            throw new UnauthorizedException("Return request " + returnId + " does not belong to seller " + sellerId);
        }

        if (!ReturnStatus.REQUESTED.equals(returnRequest.getStatus())) {
            throw new BadRequestException("Return request " + returnId + " has already been decided");
        }

        returnRequest.setStatus(approve ? ReturnStatus.APPROVED : ReturnStatus.REJECTED);
        returnRequest.setResolvedAt(LocalDateTime.now());
        returnRequest.setResolvedByRole("SELLER");

        String newSellerOrderStatus = approve ? SellerOrderStatus.RETURN_APPROVED : SellerOrderStatus.RETURN_REJECTED;
        transitionSellerOrder(sellerOrder, newSellerOrderStatus, "SELLER", sellerId, comment);

        if (approve) {
            Order order = sellerOrder.getOrder();
            Payment payment = order.getPayment();
            if (payment != null) {
                Refund refund = new Refund();
                refund.setPayment(payment);
                refund.setOrderItem(returnRequest.getOrderItem());
                refund.setAmount(returnRequest.getOrderItem().getLineTotal());
                refund.setStatus(RefundStatus.REQUESTED);
                refund.setReason("Return " + returnId + " approved: " + comment);
                refund.setRequestedByRole("SELLER");
                refund = refundRepository.save(refund);
                returnRequest.setRefund(refund);
            }
        }

        returnRequest = returnRequestRepository.save(returnRequest);
        return toDto(returnRequest);
    }

    @Override
    @Transactional
    public ReturnResponseDTO processRefund(Long refundId) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new ResourceNotFoundException("Refund not found: " + refundId));

        if (RefundStatus.COMPLETED.equals(refund.getStatus())) {
            return findReturnByRefund(refund);
        }

        refund.setStatus(RefundStatus.COMPLETED);
        refund.setProcessedAt(LocalDateTime.now());
        refund = refundRepository.save(refund);

        // Restock the returned batch via the existing addStock method, assuming
        // it is resellable — no separate "resellable" flag exists yet anywhere
        // in the product model, so this always restocks; a real "is this batch
        // still sellable" business rule would gate this call.
        OrderItem orderItem = refund.getOrderItem();
        if (orderItem != null) {
            StockInRequestDto restock = new StockInRequestDto();
            restock.setProductId(orderItem.getProductDetails().getProductId());
            restock.setPackagingId(orderItem.getPackagingIdSnapshot());
            restock.setBatchLotNumber(orderItem.getBatchLotNumberSnapshot());
            restock.setQuantity(orderItem.getQuantity().longValue());
            restock.setReferenceId(orderItem.getSellerOrder().getSellerOrderId());
            restock.setReferenceType("RETURN");
            // Same fix as OrderCancellationServiceImpl.restockItem — resolveOrCreateBatch
            // requires expiryDate to match the existing batch exactly, so it must be
            // sourced from the real batch this item was debited from, not left null.
            if (orderItem.getPricingDetails() != null) {
                restock.setManufacturingDate(orderItem.getPricingDetails().getManufacturingDate());
                restock.setExpiryDate(orderItem.getPricingDetails().getExpiryDate());
            }

            Long sellerUserId = orderItem.getSellerOrder().getSeller().getUser() != null
                    ? orderItem.getSellerOrder().getSeller().getUser().getUserId() : null;
            stockService.addStock(restock, sellerUserId);

            SellerOrder sellerOrder = orderItem.getSellerOrder();
            transitionSellerOrder(sellerOrder, SellerOrderStatus.RETURNED, "SYSTEM", null,
                    "Refund " + refundId + " completed");
        }

        return findReturnByRefund(refund);
    }

    @Override
    public ReturnResponseDTO getReturn(Long returnId) {
        ReturnRequest returnRequest = returnRequestRepository.findById(returnId)
                .orElseThrow(() -> new ResourceNotFoundException("Return request not found: " + returnId));
        return toDto(returnRequest);
    }

    private ReturnResponseDTO findReturnByRefund(Refund refund) {
        // Refund doesn't carry a back-reference to ReturnRequest (ReturnRequest
        // -> Refund is the owning direction), so scan the item's order-level
        // returns is unnecessary here — the caller (ReturnController) always
        // reaches processRefund via a returnId it already has; this helper only
        // needs to hand back the current refund/return snapshot for that flow.
        // We look the ReturnRequest up by matching orderItem, which is unique per
        // return in this simplified model.
        if (refund.getOrderItem() == null) {
            return ReturnResponseDTO.builder()
                    .refundId(refund.getRefundId())
                    .status(refund.getStatus())
                    .build();
        }
        return returnRequestRepository.findAll().stream()
                .filter(r -> r.getOrderItem() != null
                        && r.getOrderItem().getOrderItemId().equals(refund.getOrderItem().getOrderItemId())
                        && r.getRefund() != null
                        && r.getRefund().getRefundId().equals(refund.getRefundId()))
                .findFirst()
                .map(this::toDto)
                .orElseGet(() -> ReturnResponseDTO.builder()
                        .refundId(refund.getRefundId())
                        .status(refund.getStatus())
                        .build());
    }

    private void transitionSellerOrder(SellerOrder sellerOrder, String newStatus, String actorRole,
                                        String actorId, String comment) {
        String fromStatus = sellerOrder.getStatus();
        sellerOrder.setStatus(newStatus);

        // Same item-status sync as SellerOrderFulfillmentServiceImpl.transition —
        // only items still mirroring fromStatus are advanced, so an item already
        // diverged via a separate individual return isn't pulled back in sync.
        for (OrderItem item : sellerOrder.getOrderItems()) {
            if (fromStatus.equals(item.getItemStatus())) {
                item.setItemStatus(newStatus);
            }
        }

        OrderStatusHistory history = new OrderStatusHistory();
        history.setSellerOrder(sellerOrder);
        history.setFromStatus(fromStatus);
        history.setToStatus(newStatus);
        history.setChangedByRole(actorRole);
        history.setChangedById(actorId);
        history.setComment(comment);
        sellerOrder.getStatusHistory().add(history);

        sellerOrderRepository.save(sellerOrder);

        Order order = sellerOrder.getOrder();
        List<String> childStatuses = order.getSellerOrders().stream().map(SellerOrder::getStatus).toList();
        order.setStatus(OrderStatusRollup.compute(childStatuses));
        orderRepository.save(order);
    }

    private ReturnResponseDTO toDto(ReturnRequest r) {
        return ReturnResponseDTO.builder()
                .returnId(r.getReturnId())
                .orderItemId(r.getOrderItem() != null ? r.getOrderItem().getOrderItemId() : null)
                .buyerId(r.getBuyer() != null ? r.getBuyer().getBuyerId() : null)
                .reason(r.getReason())
                .status(r.getStatus())
                .requestedAt(r.getRequestedAt())
                .resolvedAt(r.getResolvedAt())
                .resolvedByRole(r.getResolvedByRole())
                .refundId(r.getRefund() != null ? r.getRefund().getRefundId() : null)
                .build();
    }
}
