package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.PackagingDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;

public interface PackagingDetailsRepository extends JpaRepository<PackagingDetails, String>{

    @Query(value = """
    SELECT MAX(CAST(SUBSTRING(packaging_id, LENGTH(packaging_id) - 4, 5) AS INTEGER))
    FROM tm_packaging_details
""", nativeQuery = true)
    Integer findMaxPackagingNumber();

    // Dedup lookup: same pack type + unit/number-of-packs + order limits under the same product
    // means this is the same packaging variant, not a new one. Locked for the same reason as
    // ProductDetailsRepository's dedup lookup — avoid concurrent duplicate inserts.
    @Lock(PESSIMISTIC_WRITE)
    Optional<PackagingDetails> findFirstByProductDetails_ProductIdAndPackType_PackIdAndUnitPerPackAndNumberOfPacksAndMinimumOrderQuantityAndMaximumOrderQuantity(
            String productId, Long packId, Long unitPerPack, Long numberOfPacks,
            Long minimumOrderQuantity, Long maximumOrderQuantity);

}
