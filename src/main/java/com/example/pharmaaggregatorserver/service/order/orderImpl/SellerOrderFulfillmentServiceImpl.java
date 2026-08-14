package com.example.pharmaaggregatorserver.service.order.orderImpl;

import com.example.pharmaaggregatorserver.dto.order.SellerOrderResponseDTO;
import com.example.pharmaaggregatorserver.entity.order.Order;
import com.example.pharmaaggregatorserver.entity.order.OrderStatusHistory;
import com.example.pharmaaggregatorserver.entity.order.SellerOrder;
import com.example.pharmaaggregatorserver.entity.order.SellerOrderStatus;
import com.example.pharmaaggregatorserver.exception.BadRequestException;
import com.example.pharmaaggregatorserver.exception.ResourceNotFoundException;
import com.example.pharmaaggregatorserver.exception.UnauthorizedException;
import com.example.pharmaaggregatorserver.repository.order.OrderRepository;
import com.example.pharmaaggregatorserver.repository.order.SellerOrderRepository;
import com.example.pharmaaggregatorserver.service.order.SellerOrderFulfillmentService;
import com.example.pharmaaggregatorserver.service.order.support.OrderMapper;
import com.example.pharmaaggregatorserver.service.order.support.OrderStatusRollup;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SellerOrderFulfillmentServiceImpl implements SellerOrderFulfillmentService {

    private final SellerOrderRepository sellerOrderRepository;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public SellerOrderResponseDTO confirm(String sellerOrderId, String sellerId) {
        return transition(sellerOrderId, sellerId, SellerOrderStatus.PLACED, SellerOrderStatus.CONFIRMED,
                so -> so.setConfirmedAt(LocalDateTime.now()));
    }

    @Override
    @Transactional
    public SellerOrderResponseDTO pack(String sellerOrderId, String sellerId) {
        return transition(sellerOrderId, sellerId, SellerOrderStatus.CONFIRMED, SellerOrderStatus.PACKED, so -> {
        });
    }

    @Override
    @Transactional
    public SellerOrderResponseDTO ship(String sellerOrderId, String sellerId, String courierName,
                                        String trackingNumber, String trackingUrl) {
        return transition(sellerOrderId, sellerId, SellerOrderStatus.PACKED, SellerOrderStatus.SHIPPED, so -> {
            so.setShippedAt(LocalDateTime.now());
            so.setCourierName(courierName);
            so.setTrackingNumber(trackingNumber);
            so.setTrackingUrl(trackingUrl);
        });
    }

    @Override
    @Transactional
    public SellerOrderResponseDTO markOutForDelivery(String sellerOrderId, String sellerId) {
        return transition(sellerOrderId, sellerId, SellerOrderStatus.SHIPPED, SellerOrderStatus.OUT_FOR_DELIVERY, so -> {
        });
    }

    @Override
    @Transactional
    public SellerOrderResponseDTO markDelivered(String sellerOrderId, String sellerId) {
        return transition(sellerOrderId, sellerId, SellerOrderStatus.OUT_FOR_DELIVERY, SellerOrderStatus.DELIVERED,
                so -> so.setDeliveredAt(LocalDateTime.now()));
    }

    private SellerOrderResponseDTO transition(String sellerOrderId, String sellerId,
                                               String requiredCurrentStatus, String newStatus,
                                               java.util.function.Consumer<SellerOrder> mutator) {
        SellerOrder sellerOrder = sellerOrderRepository.findBySellerOrderId(sellerOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller order not found: " + sellerOrderId));

        if (!sellerOrder.getSeller().getSellerId().equals(sellerId)) {
            throw new UnauthorizedException("Seller order " + sellerOrderId + " does not belong to seller " + sellerId);
        }

        if (!requiredCurrentStatus.equals(sellerOrder.getStatus())) {
            throw new BadRequestException(
                    "Seller order " + sellerOrderId + " must be " + requiredCurrentStatus
                            + " to move to " + newStatus + ", but is currently " + sellerOrder.getStatus());
        }

        String fromStatus = sellerOrder.getStatus();
        mutator.accept(sellerOrder);
        sellerOrder.setStatus(newStatus);

        // Keep each line item's status mirroring the parent SellerOrder as
        // fulfilment progresses. Only items still at fromStatus are advanced —
        // an item that has already diverged via an individual return/partial
        // cancel is left alone rather than being pulled back in sync.
        for (var item : sellerOrder.getOrderItems()) {
            if (fromStatus.equals(item.getItemStatus())) {
                item.setItemStatus(newStatus);
            }
        }

        OrderStatusHistory history = new OrderStatusHistory();
        history.setSellerOrder(sellerOrder);
        history.setFromStatus(fromStatus);
        history.setToStatus(newStatus);
        history.setChangedByRole("SELLER");
        history.setChangedById(sellerId);
        sellerOrder.getStatusHistory().add(history);

        sellerOrderRepository.save(sellerOrder);

        Order order = sellerOrder.getOrder();
        List<String> childStatuses = order.getSellerOrders().stream().map(SellerOrder::getStatus).toList();
        order.setStatus(OrderStatusRollup.compute(childStatuses));
        orderRepository.save(order);

        return orderMapper.toSellerOrderDto(sellerOrder);
    }
}
