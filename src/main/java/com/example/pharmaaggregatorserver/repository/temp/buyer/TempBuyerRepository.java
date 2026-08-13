package com.example.pharmaaggregatorserver.repository.temp.buyer;

import com.example.pharmaaggregatorserver.entity.temp.buyer.TempBuyer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TempBuyerRepository extends JpaRepository<TempBuyer, Long> {

    boolean existsByGstNumber(String gstNumber);

    boolean existsByPanNumber(String panNumber);

    Optional<TempBuyer> findByUser_BuyerUserId(Long buyerUserId);

    @Query("SELECT MAX(b.tempBuyerRequestId) FROM TempBuyer b")
    Optional<String> findMaxRequestId();
}
