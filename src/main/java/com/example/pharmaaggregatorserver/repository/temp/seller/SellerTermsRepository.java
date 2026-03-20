package com.example.pharmaaggregatorserver.repository.temp.seller;

import com.example.pharmaaggregatorserver.entity.temp.seller.SellerTerms;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SellerTermsRepository extends JpaRepository<SellerTerms, Long> {

    Optional<SellerTerms> findByTermText(String  termText);
}
