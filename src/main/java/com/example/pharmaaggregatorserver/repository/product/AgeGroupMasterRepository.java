package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.AgeGroupMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AgeGroupMasterRepository extends JpaRepository<AgeGroupMaster, Long> {
    Optional<AgeGroupMaster> findByAgeGroup(String ageGroupName);
}
