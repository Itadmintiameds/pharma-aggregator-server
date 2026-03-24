package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.ProductAttributeDrug;
import com.example.pharmaaggregatorserver.entity.product.ProductDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductAttributeDrugRepository extends JpaRepository<ProductAttributeDrug, String>{

    @Query(value = """
    SELECT MAX(CAST(SUBSTRING(product_attribute_id, LENGTH(pricing_id) - 4, 5) AS INTEGER))
    FROM tm_product_attribute_drug
""", nativeQuery = true)
    Integer findMaxPricingNumber();

}
