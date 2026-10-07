package com.example.pharmaaggregatorserver.repository.seller.history;

import com.example.pharmaaggregatorserver.entity.seller.history.SellerHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SellerHistoryRepository extends JpaRepository<SellerHistory, Long> {

    /**
     * Returns all history snapshots for a seller, newest first.
     * Use this to show a full audit trail for a given seller.
     */
    List<SellerHistory> findBySellerIdOrderBySnapshotAtDesc(String sellerId);
}
