package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.ProductDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PricingDetailsRepository extends JpaRepository<ProductDetails, String>{

    @Query(value = """
    SELECT MAX(CAST(SUBSTRING(pricing_id, LENGTH(pricing_id) - 4, 5) AS INTEGER))
    FROM tm_pricing_details
""", nativeQuery = true)
    Integer findMaxPricingNumber();

}
