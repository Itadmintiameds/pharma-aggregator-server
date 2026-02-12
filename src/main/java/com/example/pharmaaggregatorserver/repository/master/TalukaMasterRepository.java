package com.example.pharmaaggregatorserver.repository.master;

import com.example.pharmaaggregatorserver.entity.master.TalukaMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TalukaMasterRepository extends JpaRepository<TalukaMaster, Long> {
    List<TalukaMaster> findByDistrictDistrictId(Long districtId);
}