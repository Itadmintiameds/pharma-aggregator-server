package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.dto.product.TherapeuticSubcategoryDto;
import com.example.pharmaaggregatorserver.entity.product.ProductDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;

public interface ProductDetailsRepository extends JpaRepository<ProductDetails, String> {

    @Query(value = """
    SELECT MAX(CAST(SUBSTRING(product_id, LENGTH(product_id) - 4, 5) AS INTEGER))
    FROM tm_product_details
""", nativeQuery = true)
    Integer findMaxProductNumber();


    List<ProductDetails> findBySellerSellerId(String sellerId);

    // Dedup lookup used by createProduct: same seller + name + manufacturer + category means
    // this is another variant of an existing product, not a brand-new one. Locked so two
    // concurrent create calls for the same product can't both miss and insert duplicates.
    @Lock(PESSIMISTIC_WRITE)
    Optional<ProductDetails> findFirstBySeller_SellerIdAndProductNameIgnoreCaseAndManufacturerNameIgnoreCaseAndCategory_CategoryId(
            String sellerId, String productName, String manufacturerName, Long categoryId);

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
