package com.example.pharmaaggregatorserver.repository.order;

import com.example.pharmaaggregatorserver.entity.order.ReturnRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {
}
