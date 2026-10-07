package com.example.pharmaaggregatorserver.repository.order;

import com.example.pharmaaggregatorserver.entity.order.SellerOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SellerOrderRepository extends JpaRepository<SellerOrder, String> {

    Optional<SellerOrder> findBySellerOrderId(String sellerOrderId);

    List<SellerOrder> findBySeller_SellerId(String sellerId);

    List<SellerOrder> findBySeller_SellerIdAndStatus(String sellerId, String status);

    // sellerOrderId is deterministically derived from its parent orderId plus
    // an in-memory sequence within that order (see OrderPlacementServiceImpl)
    // rather than a global counter, so — unlike Order/Payment — no advisory
    // lock is needed for generating it.
}
