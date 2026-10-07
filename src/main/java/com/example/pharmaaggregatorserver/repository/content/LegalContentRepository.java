package com.example.pharmaaggregatorserver.repository.content;

import com.example.pharmaaggregatorserver.entity.content.LegalContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LegalContentRepository extends JpaRepository<LegalContent, Long> {

    Optional<LegalContent> findByContentKeyAndIsActiveTrue(String contentKey);
}
