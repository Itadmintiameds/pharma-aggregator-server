package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.dto.product.StorageConditionDropdownDTO;
import com.example.pharmaaggregatorserver.entity.product.MedicalDeviceProductMaster.StorageConditionMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StorageConditionMasterRepository extends JpaRepository<StorageConditionMaster, Long> {
//    Optional<StorageConditionMaster> findByConditionName(String storage);

    Optional<StorageConditionMaster> findByConditionNameAndCategory_CategoryId(String storage, Long categoryId);

    Optional<StorageConditionMaster> findByConditionNameIgnoreCaseAndCategory_CategoryId(String storage, Long categoryId);

    @Query("""
                SELECT s.storageConditionId AS storageConditionId,
                       s.conditionName AS conditionName
                FROM StorageConditionMaster s
                WHERE s.category.categoryId = :categoryId
                ORDER BY s.displayOrder ASC
            """)
    List<StorageConditionDropdownDTO> findByCategoryId(Long categoryId);
}
