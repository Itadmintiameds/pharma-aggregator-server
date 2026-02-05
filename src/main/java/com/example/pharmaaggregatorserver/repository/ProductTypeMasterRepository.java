package com.example.pharmaaggregatorserver.repository;

import com.example.pharmaaggregatorserver.entity.ProductTypeMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductTypeMasterRepository extends JpaRepository<ProductTypeMaster, Integer> {
    // findAll() is already provided by JpaRepository
}