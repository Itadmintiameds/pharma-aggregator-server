package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.PackagingDetails;
import com.example.pharmaaggregatorserver.entity.product.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductImageRepository extends JpaRepository<ProductImage, String>{

    @Query(value = """
    SELECT MAX(CAST(SUBSTRING(product_image_id, LENGTH(packaging_id) - 4, 5) AS INTEGER))
    FROM tm_product_image
""", nativeQuery = true)
    Integer findMaxPackagingNumber();

}
