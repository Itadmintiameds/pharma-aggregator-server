package com.example.pharmaaggregatorserver.repository;

import com.example.pharmaaggregatorserver.entity.CompanyTypeMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyTypeMasterRepository extends JpaRepository<CompanyTypeMaster, Integer> {
    // findAll() is already provided by JpaRepository
}