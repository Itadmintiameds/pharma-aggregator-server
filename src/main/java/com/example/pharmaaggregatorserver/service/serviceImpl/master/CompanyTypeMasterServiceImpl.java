package com.example.pharmaaggregatorserver.service.serviceImpl.master;

import com.example.pharmaaggregatorserver.entity.master.CompanyTypeMaster;
import com.example.pharmaaggregatorserver.repository.temp.master.CompanyTypeMasterRepository;
import com.example.pharmaaggregatorserver.service.master.CompanyTypeMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyTypeMasterServiceImpl implements CompanyTypeMasterService {

    private final CompanyTypeMasterRepository companyTypeMasterRepository;

    // Get all company types
    @Override
    public List<CompanyTypeMaster> getAllCompanyTypes() {
        return companyTypeMasterRepository.findAll();
    }
}