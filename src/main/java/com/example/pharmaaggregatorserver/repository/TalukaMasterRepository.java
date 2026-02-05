package com.example.pharmaaggregatorserver.repository;

import com.example.pharmaaggregatorserver.entity.TalukaMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TalukaMasterRepository extends JpaRepository<TalukaMaster, Integer> {

    // Find talukas by district ID
    List<TalukaMaster> findByDistrictId(Integer districtId);
}