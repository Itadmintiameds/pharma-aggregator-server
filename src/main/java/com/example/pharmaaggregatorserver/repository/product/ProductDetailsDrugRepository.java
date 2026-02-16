package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.ProductDetailsDrug;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductDetailsDrugRepository extends JpaRepository<ProductDetailsDrug, Long> {

    @Query(value = """
    SELECT MAX(CAST(SUBSTRING(product_id, LENGTH(product_id) - 4, 5) AS INTEGER))
    FROM pm_product_details_drug
""", nativeQuery = true)
    Integer findMaxProductNumber();


}
