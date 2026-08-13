package com.example.pharmaaggregatorserver.repository.buyer;

import com.example.pharmaaggregatorserver.entity.buyer.BuyerDeliveryAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BuyerDeliveryAddressRepository extends JpaRepository<BuyerDeliveryAddress, Long> {
}
