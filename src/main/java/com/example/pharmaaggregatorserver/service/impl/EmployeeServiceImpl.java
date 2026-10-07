package com.example.pharmaaggregatorserver.service.impl;


import com.example.pharmaaggregatorserver.dto.EmployeeDto;
import com.example.pharmaaggregatorserver.entity.Employee;
import com.example.pharmaaggregatorserver.exception.ResourceNotFoundException;
import com.example.pharmaaggregatorserver.mapper.EmployeeMapper;
import com.example.pharmaaggregatorserver.repository.EmployeeRepository;
import com.example.pharmaaggregatorserver.service.EmployeeService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;



import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
    private EmployeeRepository employeeRepository;
    @Override
    public EmployeeDto createEmployee(EmployeeDto employeeDto) {
        Employee employee= EmployeeMapper.mapToEmployee(employeeDto);
        Employee saveEmployee = employeeRepository.save(employee);
        return EmployeeMapper.mapToEmployeeDto(saveEmployee);
    }

    @Override
    public EmployeeDto getEmployeeById(long employeeId) {
        Employee employee= employeeRepository.findById(employeeId)
                .orElseThrow(()-> new ResourceNotFoundException("Employee is not exist with given id :"+employeeId));
        return EmployeeMapper.mapToEmployeeDto(employee);
    }

    @Override
    public List<EmployeeDto> getAllEmployees() {
        List<Employee> employees = employeeRepository.findAll();
        return employees.stream().map((employee)-> EmployeeMapper.mapToEmployeeDto(employee))
                .collect(Collectors.toList());
    }

    @Override
    public EmployeeDto updateEmployee(long employeeId, EmployeeDto updateemployeeDto) {
        Employee employee= employeeRepository.findById(employeeId).orElseThrow(()->new ResourceNotFoundException("Employee is not exists with given id:"+employeeId));
        employee.setFirstName(updateemployeeDto.getFirstName());
        employee.setLastName(updateemployeeDto.getLastName());
        employee.setEmail(updateemployeeDto.getEmail());
        Employee updatedEmployee = employeeRepository.save(employee);
        return EmployeeMapper.mapToEmployeeDto(updatedEmployee);
    }

    @Override
    public void deleteEmployee(long employeeId) {
        Employee employee= employeeRepository.findById(employeeId).orElseThrow(()->new ResourceNotFoundException("Employee is not exists with given id:"+employeeId));
        employeeRepository.deleteById(employeeId);
    }
}
