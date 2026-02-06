package com.example.pharmaaggregatorserver.repository.temp.master;

import com.example.pharmaaggregatorserver.entity.master.DistrictMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface DistrictMasterRepository extends JpaRepository<DistrictMaster, Integer> {

    // Find districts by state ID
    List<DistrictMaster> findByState_StateId(Integer stateId);

    // Find active districts by state ID
    List<DistrictMaster> findByState_StateIdAndIsActiveTrue(Integer stateId);
}
