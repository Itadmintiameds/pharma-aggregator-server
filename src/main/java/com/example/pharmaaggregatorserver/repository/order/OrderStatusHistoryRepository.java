package com.example.pharmaaggregatorserver.repository.order;

import com.example.pharmaaggregatorserver.entity.order.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {

    List<OrderStatusHistory> findBySellerOrder_SellerOrderIdOrderByChangedAtAsc(String sellerOrderId);
}
