package com.example.pharmaaggregatorserver.service.serviceImpl.master;


import com.example.pharmaaggregatorserver.dto.master.ResponseDTO.CompanyTypeResponseDTO;
import com.example.pharmaaggregatorserver.entity.master.CompanyTypeMaster;
import com.example.pharmaaggregatorserver.repository.master.CompanyTypeMasterRepository;
import com.example.pharmaaggregatorserver.service.master.CompanyTypeMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyTypeMasterServiceImpl implements CompanyTypeMasterService {

    private final CompanyTypeMasterRepository companyTypeMasterRepository;

    private CompanyTypeResponseDTO convertToResponseDTO(CompanyTypeMaster entity) {
        return new CompanyTypeResponseDTO(
                entity.getCompanyTypeId(),
                entity.getCompanyTypeName(),
                entity.getIsActive()
        );
    }

    @Override
    public List<CompanyTypeResponseDTO> getAllCompanyTypes() {
        return companyTypeMasterRepository.findAll()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }
}
