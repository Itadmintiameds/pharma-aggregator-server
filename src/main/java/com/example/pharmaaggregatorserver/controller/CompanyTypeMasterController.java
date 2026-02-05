package com.example.pharmaaggregatorserver.controller;

import com.example.pharmaaggregatorserver.entity.CompanyTypeMaster;
import com.example.pharmaaggregatorserver.service.CompanyTypeMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/company-types")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CompanyTypeMasterController {

    private final CompanyTypeMasterService companyTypeMasterService;

    /**
     * GET /api/v1/company-types
     * Get all company types
     */
    @GetMapping
    public ResponseEntity<List<CompanyTypeMaster>> getAllCompanyTypes() {
        List<CompanyTypeMaster> companyTypes = companyTypeMasterService.getAllCompanyTypes();
        return ResponseEntity.ok(companyTypes);
    }
}