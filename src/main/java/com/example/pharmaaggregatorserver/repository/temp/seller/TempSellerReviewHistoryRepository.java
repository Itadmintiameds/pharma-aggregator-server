package com.example.pharmaaggregatorserver.repository.temp.seller;

import com.example.pharmaaggregatorserver.entity.temp.seller.TempSeller;
import com.example.pharmaaggregatorserver.entity.temp.seller.TempSellerReviewHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TempSellerReviewHistoryRepository extends JpaRepository<TempSellerReviewHistory, Long> {
    List<TempSellerReviewHistory> findByTempSellerOrderByReviewedAtAsc(TempSeller tempSeller);
}
