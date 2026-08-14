package com.example.pharmaaggregatorserver.service.order.orderImpl;

import com.example.pharmaaggregatorserver.dto.order.OrderResponseDTO;
import com.example.pharmaaggregatorserver.dto.order.SellerOrderResponseDTO;
import com.example.pharmaaggregatorserver.entity.order.Order;
import com.example.pharmaaggregatorserver.entity.order.OrderStatusHistory;
import com.example.pharmaaggregatorserver.entity.order.SellerOrder;
import com.example.pharmaaggregatorserver.exception.BadRequestException;
import com.example.pharmaaggregatorserver.exception.ResourceNotFoundException;
import com.example.pharmaaggregatorserver.repository.order.OrderRepository;
import com.example.pharmaaggregatorserver.repository.order.SellerOrderRepository;
import com.example.pharmaaggregatorserver.service.order.OrderQueryService;
import com.example.pharmaaggregatorserver.service.order.support.OrderMapper;
import com.example.pharmaaggregatorserver.service.order.support.OrderStatusRollup;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderQueryServiceImpl implements OrderQueryService {

    private final OrderRepository orderRepository;
    private final SellerOrderRepository sellerOrderRepository;
    private final OrderMapper orderMapper;

    @Override
    public OrderResponseDTO getOrder(String orderId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        return orderMapper.toOrderDto(order);
    }

    @Override
    public List<OrderResponseDTO> getOrdersByBuyer(String buyerId) {
        return orderRepository.findByBuyer_BuyerId(buyerId).stream().map(orderMapper::toOrderDto).toList();
    }

    @Override
    public List<OrderResponseDTO> getAllOrders(String status) {
        List<Order> orders = (status == null || status.isBlank())
                ? orderRepository.findAll()
                : orderRepository.findByStatus(status);
        return orders.stream().map(orderMapper::toOrderDto).toList();
    }

    @Override
    public SellerOrderResponseDTO getSellerOrder(String sellerOrderId) {
        SellerOrder sellerOrder = sellerOrderRepository.findBySellerOrderId(sellerOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller order not found: " + sellerOrderId));
        return orderMapper.toSellerOrderDto(sellerOrder);
    }

    @Override
    public List<SellerOrderResponseDTO> getSellerOrdersBySeller(String sellerId, String status) {
        List<SellerOrder> sellerOrders = (status == null || status.isBlank())
                ? sellerOrderRepository.findBySeller_SellerId(sellerId)
                : sellerOrderRepository.findBySeller_SellerIdAndStatus(sellerId, status);
        return sellerOrders.stream().map(orderMapper::toSellerOrderDto).toList();
    }

    @Override
    @Transactional
    public OrderResponseDTO adminOverride(String orderId, String newStatus, String reason, String adminId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        if (order.getSellerOrders().isEmpty()) {
            throw new BadRequestException("Order " + orderId + " has no seller orders to override");
        }

        for (SellerOrder sellerOrder : order.getSellerOrders()) {
            String fromStatus = sellerOrder.getStatus();
            sellerOrder.setStatus(newStatus);

            OrderStatusHistory history = new OrderStatusHistory();
            history.setSellerOrder(sellerOrder);
            history.setFromStatus(fromStatus);
            history.setToStatus(newStatus);
            history.setChangedByRole("ADMIN");
            history.setChangedById(adminId);
            history.setComment(reason);
            sellerOrder.getStatusHistory().add(history);

            sellerOrderRepository.save(sellerOrder);
        }

        List<String> childStatuses = order.getSellerOrders().stream().map(SellerOrder::getStatus).toList();
        order.setStatus(OrderStatusRollup.compute(childStatuses));
        order = orderRepository.save(order);

        return orderMapper.toOrderDto(order);
    }
}
