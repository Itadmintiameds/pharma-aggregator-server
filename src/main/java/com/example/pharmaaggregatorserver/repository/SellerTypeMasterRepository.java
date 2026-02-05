package com.example.pharmaaggregatorserver.repository;

import com.example.pharmaaggregatorserver.entity.SellerTypeMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SellerTypeMasterRepository extends JpaRepository<SellerTypeMaster, Integer> {
    // findAll() is already provided by JpaRepository
}