package com.example.pharmaaggregatorserver.controller.master;

import com.example.pharmaaggregatorserver.dto.master.ResponseDTO.CompanyTypeResponseDTO;
import com.example.pharmaaggregatorserver.service.master.CompanyTypeMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/company-types")
@RequiredArgsConstructor
public class CompanyTypeMasterController {

    private final CompanyTypeMasterService companyTypeMasterService;

    @GetMapping
    public ResponseEntity<List<CompanyTypeResponseDTO>> getAllCompanyTypes() {
        return ResponseEntity.ok(companyTypeMasterService.getAllCompanyTypes());
    }
}
