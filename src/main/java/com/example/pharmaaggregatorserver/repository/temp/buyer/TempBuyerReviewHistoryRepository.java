package com.example.pharmaaggregatorserver.repository.temp.buyer;

import com.example.pharmaaggregatorserver.entity.temp.buyer.TempBuyer;
import com.example.pharmaaggregatorserver.entity.temp.buyer.TempBuyerReviewHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TempBuyerReviewHistoryRepository extends JpaRepository<TempBuyerReviewHistory, Long> {
    List<TempBuyerReviewHistory> findByTempBuyerOrderByReviewedAtAsc(TempBuyer tempBuyer);
}
