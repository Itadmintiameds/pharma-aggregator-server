package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.PackagingDetailsDrug;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PackagingDetailsDrugRepository extends JpaRepository<PackagingDetailsDrug, Long>{

    @Query(value = """
    SELECT MAX(CAST(SUBSTRING(packaging_id, LENGTH(packaging_id) - 4, 5) AS INTEGER))
    FROM pm_packaging_details_drug
""", nativeQuery = true)
    Integer findMaxPackagingNumber();

}
