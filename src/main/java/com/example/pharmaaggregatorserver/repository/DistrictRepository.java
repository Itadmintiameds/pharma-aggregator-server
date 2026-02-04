package com.example.pharmaaggregatorserver.repository;

import com.example.pharmaaggregatorserver.entity.DistrictEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DistrictRepository extends JpaRepository<DistrictEntity, Long> {

    List<DistrictEntity> findAllByState_Id(Long stateId);
}
