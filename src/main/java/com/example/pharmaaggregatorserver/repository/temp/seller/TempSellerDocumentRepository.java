package com.example.pharmaaggregatorserver.repository.temp.seller;

import com.example.pharmaaggregatorserver.entity.temp.seller.TempSellerDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TempSellerDocumentRepository extends JpaRepository<TempSellerDocument, Long> {
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM TempSellerDocument t WHERE t.documentNumber = :documentNumber")
    boolean existsByDocumentNumber(@Param("documentNumber") String documentNumber);
}
