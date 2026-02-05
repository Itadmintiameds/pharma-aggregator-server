package com.example.pharmaaggregatorserver.service;

import com.example.pharmaaggregatorserver.entity.DistrictMaster;
import com.example.pharmaaggregatorserver.repository.DistrictMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DistrictMasterService {

    private final DistrictMasterRepository districtMasterRepository;

    // Get all districts
    public List<DistrictMaster> getAllDistricts() {
        List<DistrictMaster> districts = districtMasterRepository.findAll();
        if (districts.isEmpty()) {
            return List.of();
        }
        return districts;
    }

    // Get districts by state ID
    public List<DistrictMaster> getDistrictsByStateId(Integer stateId) {
        List<DistrictMaster> districts = districtMasterRepository.findByStateId(stateId);
        if (districts.isEmpty()) {
            return List.of();
        }
        return districts;
    }

    // Get active districts by state ID
    public List<DistrictMaster> getActiveDistrictsByStateId(Integer stateId) {
        List<DistrictMaster> districts = districtMasterRepository.findByStateIdAndIsActiveTrue(stateId);
        if (districts.isEmpty()) {
            return List.of();
        }
        return districts;
    }
}