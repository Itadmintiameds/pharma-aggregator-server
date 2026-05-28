package com.example.pharmaaggregatorserver.service.product;

public interface PricingDetailsService {

    boolean isBatchNumberExistsForSeller(String batchLotNumber, Long sellerId, Long categoryId);

}
