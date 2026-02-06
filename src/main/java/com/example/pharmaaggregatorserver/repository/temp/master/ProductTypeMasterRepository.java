package com.example.pharmaaggregatorserver.repository.temp.master;

import com.example.pharmaaggregatorserver.entity.master.ProductTypeMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface ProductTypeMasterRepository extends JpaRepository<ProductTypeMaster, Integer> {
    // findAll() is already provided by JpaRepository
}