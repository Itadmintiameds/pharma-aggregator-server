package com.example.pharmaaggregatorserver.repository.order;

import com.example.pharmaaggregatorserver.entity.order.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundRepository extends JpaRepository<Refund, Long> {
}
