package com.example.pharmaaggregatorserver.service.master;

import com.example.pharmaaggregatorserver.dto.master.ResponseDTO.CompanyTypeResponseDTO;

import java.util.List;

public interface CompanyTypeMasterService {
    List<CompanyTypeResponseDTO> getAllCompanyTypes();
}