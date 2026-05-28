package com.example.pharmaaggregatorserver.service.product.productImpl;

import com.example.pharmaaggregatorserver.repository.product.PricingDetailsRepository;
import com.example.pharmaaggregatorserver.service.product.PricingDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PricingDetailsServiceImpl implements PricingDetailsService {

    private final PricingDetailsRepository pricingDetailsRepository;

    @Override
    public boolean isBatchNumberExistsForSeller(
            String batchLotNumber,
            Long sellerId,
            Long categoryId
    ) {

        return pricingDetailsRepository
                .existsByBatchLotNumberAndUserIdAndCategoryId(
                        batchLotNumber,
                        sellerId,
                        categoryId
                );
    }
}
