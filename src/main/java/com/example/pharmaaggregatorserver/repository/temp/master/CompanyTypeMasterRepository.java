package com.example.pharmaaggregatorserver.repository.temp.master;

import com.example.pharmaaggregatorserver.entity.master.CompanyTypeMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface CompanyTypeMasterRepository extends JpaRepository<CompanyTypeMaster, Integer> {
    // findAll() is already provided by JpaRepository
}