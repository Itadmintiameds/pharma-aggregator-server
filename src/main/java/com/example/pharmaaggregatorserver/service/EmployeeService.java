package com.example.pharmaaggregatorserver.service;

import com.example.pharmaaggregatorserver.dto.EmployeeDto;


import java.util.List;

public interface EmployeeService {
    EmployeeDto createEmployee(EmployeeDto employeeDto);

    EmployeeDto getEmployeeById(long employeeId);

    List<EmployeeDto> getAllEmployees();

    EmployeeDto updateEmployee(long employeeId, EmployeeDto employeeDto);

    void deleteEmployee(long employeeId);
}