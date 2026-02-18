package com.example.pharmaaggregatorserver.repository.temp.seller;

import com.example.pharmaaggregatorserver.entity.temp.seller.TempSellerBankDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TempSellerBankDetailsRepository extends JpaRepository<TempSellerBankDetails, Long> {
    Optional<TempSellerBankDetails> findBySeller_TempSellerId(Long tempSellerId);
}
