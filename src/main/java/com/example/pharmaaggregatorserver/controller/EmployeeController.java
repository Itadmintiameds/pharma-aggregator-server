package com.example.pharmaaggregatorserver.controller;
import com.example.pharmaaggregatorserver.dto.EmployeeDto;
import com.example.pharmaaggregatorserver.service.EmployeeService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@CrossOrigin("*")
@RestController
@RequestMapping("/api/employees")
@AllArgsConstructor
public class EmployeeController {
    private EmployeeService employeeService;

    //Build Add Employee REST API
    @PostMapping
    public ResponseEntity<EmployeeDto> createEmployee(@RequestBody EmployeeDto employeeDto){
        EmployeeDto savedEmployee = employeeService.createEmployee(employeeDto);
        return new ResponseEntity<>(savedEmployee, HttpStatus.CREATED);
    }

    //Build GET Employee REST API
    @GetMapping("{id}")
    public ResponseEntity<EmployeeDto> getEmployeeById(@PathVariable("id") long employeeId){
        EmployeeDto employeeDto= employeeService.getEmployeeById(employeeId);
        return ResponseEntity.ok(employeeDto);
    }

    //Build GET ALL Employee REST API
    @GetMapping
    public ResponseEntity<List> getAllEmployee(){
        List<EmployeeDto> employeeDto= employeeService.getAllEmployees();
        return ResponseEntity.ok(employeeDto);
    }

    //Build Update Employee REST API
    @PutMapping("{id}")
    public ResponseEntity<EmployeeDto> updateEmployee(@PathVariable("id") Long EmployeeId,@RequestBody EmployeeDto updatedEmployee){
        EmployeeDto employeeDto = employeeService.updateEmployee(EmployeeId,updatedEmployee);
        return ResponseEntity.ok(employeeDto);
    }

    //Build Delete Employee REST API
    @DeleteMapping("{id}")
    public ResponseEntity<String> deleteEmployee(@PathVariable("id") Long EmployeeId){
        employeeService.deleteEmployee(EmployeeId);
        return ResponseEntity.ok("Employee Deleted Successfully..!");
    }
}
