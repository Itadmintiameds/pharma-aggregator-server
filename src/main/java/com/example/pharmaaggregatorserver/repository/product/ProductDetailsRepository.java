package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.ProductDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductDetailsRepository extends JpaRepository<ProductDetails, String> {

    @Query(value = """
    SELECT MAX(CAST(SUBSTRING(product_id, LENGTH(product_id) - 4, 5) AS INTEGER))
    FROM tm_product_details
""", nativeQuery = true)
    Integer findMaxProductNumber();


}
