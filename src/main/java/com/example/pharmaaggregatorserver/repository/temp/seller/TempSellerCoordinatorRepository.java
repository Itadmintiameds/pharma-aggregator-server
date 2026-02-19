package com.example.pharmaaggregatorserver.repository.temp.seller;

import com.example.pharmaaggregatorserver.entity.temp.seller.TempSellerCoordinator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TempSellerCoordinatorRepository extends JpaRepository<TempSellerCoordinator, Long> {
    boolean existsByEmail(String email);
    // Phone methods
    boolean existsByMobile(String mobile);

}