package com.example.pharmaaggregatorserver.repository.temp.master;

import com.example.pharmaaggregatorserver.entity.master.StateMaster;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StateMasterRepository extends JpaRepository<StateMaster, Integer> {
    // Only basic CRUD methods from JpaRepository are needed
    // findAll() is already provided by JpaRepository
}