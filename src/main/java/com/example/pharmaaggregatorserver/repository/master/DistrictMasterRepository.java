package com.example.pharmaaggregatorserver.repository.master;

import com.example.pharmaaggregatorserver.entity.master.DistrictMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DistrictMasterRepository extends JpaRepository<DistrictMaster, Long> {
    List<DistrictMaster> findByStateStateId(Long stateId);
}
