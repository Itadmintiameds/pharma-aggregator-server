package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.PackagingDetails;
import com.example.pharmaaggregatorserver.entity.product.PackagingDetailsDrug;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PackagingDetailsRepository extends JpaRepository<PackagingDetails, String>{

    @Query(value = """
    SELECT MAX(CAST(SUBSTRING(packaging_id, LENGTH(packaging_id) - 4, 5) AS INTEGER))
    FROM tm_packaging_details
""", nativeQuery = true)
    Integer findMaxPackagingNumber();

}
