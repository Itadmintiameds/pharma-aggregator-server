package com.example.pharmaaggregatorserver.repository.master;

import com.example.pharmaaggregatorserver.entity.master.BuyerTypeMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BuyerTypeMasterRepository extends JpaRepository<BuyerTypeMaster, Long> {
    List<BuyerTypeMaster> findAllByIsActiveTrue();
}
