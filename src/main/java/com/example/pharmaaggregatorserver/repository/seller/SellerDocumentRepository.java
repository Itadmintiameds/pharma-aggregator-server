package com.example.pharmaaggregatorserver.repository.seller;

import com.example.pharmaaggregatorserver.entity.seller.SellerDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerDocumentRepository extends JpaRepository<SellerDocument, Long> {
    boolean existsByDocumentNumber(String documentnumber);
}
