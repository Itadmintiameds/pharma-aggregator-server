package com.example.pharmaaggregatorserver.service.master;

import com.example.pharmaaggregatorserver.entity.master.CompanyTypeMaster;

import java.util.List;

public interface CompanyTypeMasterService {

    // Get all company types
    public List<CompanyTypeMaster> getAllCompanyTypes();
}