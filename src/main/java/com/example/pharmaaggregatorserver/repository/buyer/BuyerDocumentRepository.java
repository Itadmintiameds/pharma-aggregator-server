package com.example.pharmaaggregatorserver.repository.buyer;

import com.example.pharmaaggregatorserver.entity.buyer.BuyerDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BuyerDocumentRepository extends JpaRepository<BuyerDocument, Long> {
}
