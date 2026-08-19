package com.example.pharmaaggregatorserver.repository.quote;

import com.example.pharmaaggregatorserver.entity.quote.QuoteRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuoteRequestRepository extends JpaRepository<QuoteRequest, Long> {

    List<QuoteRequest> findByBuyerUser_BuyerUserIdOrderByCreatedAtDesc(Long buyerUserId);

    List<QuoteRequest> findBySeller_SellerIdOrderByCreatedAtDesc(String sellerId);

    Optional<QuoteRequest> findByQuoteRequestIdAndSeller_SellerId(Long quoteRequestId, String sellerId);

    Optional<QuoteRequest> findByQuoteRequestIdAndBuyerUser_BuyerUserId(Long quoteRequestId, Long buyerUserId);
}
