package com.example.pharmaaggregatorserver.repository;

import com.example.pharmaaggregatorserver.entity.TalukaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TalukaRepository extends JpaRepository<TalukaEntity, Long> {

    List<TalukaEntity> findAllByDistrict_Id(Long districtId);
}
