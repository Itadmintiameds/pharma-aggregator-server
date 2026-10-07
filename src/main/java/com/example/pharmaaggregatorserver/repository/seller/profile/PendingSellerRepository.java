package com.example.pharmaaggregatorserver.repository.seller.profile;

import com.example.pharmaaggregatorserver.entity.seller.profile.PendingSeller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PendingSellerRepository extends JpaRepository<PendingSeller, Long> {
    List<PendingSeller> findByStatus(String status);
    List<PendingSeller> findBySellerIdAndStatus(String sellerId, String status);
    List<PendingSeller> findByStatusAndCreatedAtBefore(String status, LocalDateTime cutoffDate);
}