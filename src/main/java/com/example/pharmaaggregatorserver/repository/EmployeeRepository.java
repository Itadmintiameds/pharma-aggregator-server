package com.example.pharmaaggregatorserver.repository;

import com.example.pharmaaggregatorserver.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;


public interface EmployeeRepository extends JpaRepository<Employee,Long> {
}
