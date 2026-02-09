package com.example.pharmaaggregatorserver.service.master;

import com.example.pharmaaggregatorserver.dto.master.ResponseDTO.DistrictResponseDTO;

import java.util.List;

public interface DistrictMasterService {
    List<DistrictResponseDTO> getAllDistricts();
}