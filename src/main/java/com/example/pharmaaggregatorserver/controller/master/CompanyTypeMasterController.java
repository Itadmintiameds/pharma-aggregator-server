package com.example.pharmaaggregatorserver.controller.master;

import com.example.pharmaaggregatorserver.entity.master.CompanyTypeMaster;
import com.example.pharmaaggregatorserver.service.serviceImpl.master.CompanyTypeMasterServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/company-types")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CompanyTypeMasterController {

    private final CompanyTypeMasterServiceImpl companyTypeMasterService;

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