package com.example.pharmaaggregatorserver.service;

import com.example.pharmaaggregatorserver.entity.CompanyTypeMaster;
import com.example.pharmaaggregatorserver.repository.CompanyTypeMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyTypeMasterService {

    private final CompanyTypeMasterRepository companyTypeMasterRepository;

    // Get all company types
    public List<CompanyTypeMaster> getAllCompanyTypes() {
        return companyTypeMasterRepository.findAll();
    }
}