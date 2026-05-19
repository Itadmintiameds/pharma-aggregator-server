package com.example.pharmaaggregatorserver.repository.temp.seller;

import com.example.pharmaaggregatorserver.entity.temp.seller.TempSellerCoordinator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TempSellerCoordinatorRepository extends JpaRepository<TempSellerCoordinator, Long> {
    boolean existsByEmail(String email);
    // Phone methods
    boolean existsByMobile(String mobile);
    Optional<TempSellerCoordinator> findBySeller_TempSellerId(Long tempSellerId);
}