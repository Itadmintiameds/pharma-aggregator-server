package com.example.pharmaaggregatorserver.repository.seller.profile;

import com.example.pharmaaggregatorserver.entity.seller.profile.PendingSellerDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PendingSellerDocumentRepository extends JpaRepository<PendingSellerDocument, Long> {
}