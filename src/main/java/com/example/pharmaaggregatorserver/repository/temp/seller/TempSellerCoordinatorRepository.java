package com.example.pharmaaggregatorserver.repository.temp.seller;

import com.example.pharmaaggregatorserver.entity.temp.seller.TempSellerCoordinator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TempSellerCoordinatorRepository extends JpaRepository<TempSellerCoordinator, Long> {
    boolean existsByEmail(String email);
    // Excludes the caller's own in-progress draft so re-visiting a step they
    // already saved (e.g. going back and continuing again) doesn't flag
    // their own previously-saved coordinator email as a duplicate.
    boolean existsByEmailAndSeller_TempSellerIdNot(String email, Long tempSellerId);
    // Phone methods
    boolean existsByMobile(String mobile);
    boolean existsByMobileAndSeller_TempSellerIdNot(String mobile, Long tempSellerId);
    Optional<TempSellerCoordinator> findBySeller_TempSellerId(Long tempSellerId);
}