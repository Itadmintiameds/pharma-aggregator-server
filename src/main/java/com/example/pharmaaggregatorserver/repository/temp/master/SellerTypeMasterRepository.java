package com.example.pharmaaggregatorserver.repository.temp.master;

import com.example.pharmaaggregatorserver.entity.master.SellerTypeMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface SellerTypeMasterRepository extends JpaRepository<SellerTypeMaster, Integer> {
    // findAll() is already provided by JpaRepository
}