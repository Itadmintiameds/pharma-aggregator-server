package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.dto.product.TherapeuticSubcategoryDto;
import com.example.pharmaaggregatorserver.entity.product.ProductDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductDetailsRepository extends JpaRepository<ProductDetails, String> {

    @Query(value = """
    SELECT MAX(CAST(SUBSTRING(product_id, LENGTH(product_id) - 4, 5) AS INTEGER))
    FROM tm_product_details
""", nativeQuery = true)
    Integer findMaxProductNumber();


    List<ProductDetails> findBySellerSellerId(String sellerId);

    @Query(
            value = """
        SELECT 
            subcategory_id AS subcategoryId,
            subcategory_name AS subcategoryName
        FROM pm_product_therapeuticsubcategory_drug
        WHERE category_id = :categoryId
        ORDER BY subcategory_id
    """,
            nativeQuery = true
    )
    List<TherapeuticSubcategoryDto> findByCategoryId(String categoryId);


}
