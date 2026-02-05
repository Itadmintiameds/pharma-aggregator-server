package com.example.pharmaaggregatorserver.repository;

import com.example.pharmaaggregatorserver.entity.DistrictMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DistrictMasterRepository extends JpaRepository<DistrictMaster, Integer> {

    // Find districts by state ID
    List<DistrictMaster> findByStateId(Integer stateId);

    // Find active districts by state ID
    List<DistrictMaster> findByStateIdAndIsActiveTrue(Integer stateId);
}
