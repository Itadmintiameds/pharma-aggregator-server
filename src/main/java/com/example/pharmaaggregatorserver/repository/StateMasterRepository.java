package com.example.pharmaaggregatorserver.repository;

import com.example.pharmaaggregatorserver.entity.StateMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StateMasterRepository extends JpaRepository<StateMaster, Integer> {
    // Only basic CRUD methods from JpaRepository are needed
    // findAll() is already provided by JpaRepository
}