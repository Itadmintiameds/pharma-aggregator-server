package com.example.pharmaaggregatorserver.repository.temp.buyer;

import com.example.pharmaaggregatorserver.entity.temp.buyer.TempBuyerDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TempBuyerDocumentRepository extends JpaRepository<TempBuyerDocument, Long> {
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM TempBuyerDocument t WHERE t.documentNumber = :documentNumber")
    boolean existsByDocumentNumber(@Param("documentNumber") String documentNumber);
}
