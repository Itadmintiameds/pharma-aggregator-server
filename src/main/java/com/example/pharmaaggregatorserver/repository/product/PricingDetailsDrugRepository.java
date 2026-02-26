package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.PricingDetailsDrug;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PricingDetailsDrugRepository extends JpaRepository<PricingDetailsDrug, String>{

    @Query(value = """
    SELECT MAX(CAST(SUBSTRING(pricing_id, LENGTH(pricing_id) - 4, 5) AS INTEGER))
    FROM pm_pricing_details_drug
""", nativeQuery = true)
    Integer findMaxPricingNumber();

}
