package com.example.pharmaaggregatorserver.repository.seller;

import com.example.pharmaaggregatorserver.entity.seller.SellerCoordinator;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerCoordinatorRepository extends JpaRepository<SellerCoordinator, Long> {
    boolean existsByEmail(String email);
    // Phone methods
    boolean existsByMobile(String mobile);

}
