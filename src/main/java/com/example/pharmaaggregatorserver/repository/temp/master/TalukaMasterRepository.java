package com.example.pharmaaggregatorserver.repository.temp.master;

import com.example.pharmaaggregatorserver.entity.master.TalukaMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface TalukaMasterRepository extends JpaRepository<TalukaMaster, Integer> {

    // Find talukas by district ID
    List<TalukaMaster> findByDistrict_DistrictId(Integer districtId);
}