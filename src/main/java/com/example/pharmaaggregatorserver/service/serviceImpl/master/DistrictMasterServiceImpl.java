package com.example.pharmaaggregatorserver.service.serviceImpl.master;

import com.example.pharmaaggregatorserver.entity.master.DistrictMaster;
import com.example.pharmaaggregatorserver.repository.temp.master.DistrictMasterRepository;
import com.example.pharmaaggregatorserver.service.master.DistrictMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DistrictMasterServiceImpl implements DistrictMasterService {

    private final DistrictMasterRepository districtMasterRepository;

    // Get all districts
    @Override
    public List<DistrictMaster> getAllDistricts() {
        List<DistrictMaster> districts = districtMasterRepository.findAll();
        if (districts.isEmpty()) {
            return List.of();
        }
        return districts;
    }

    // Get districts by state ID
    @Override
    public List<DistrictMaster> getDistrictsByStateId(Integer stateId) {
        List<DistrictMaster> districts = districtMasterRepository.findByState_StateId(stateId);
        if (districts.isEmpty()) {
            return List.of();
        }
        return districts;
    }

    // Get active districts by state ID
    @Override
    public List<DistrictMaster> getActiveDistrictsByStateId(Integer stateId) {
        List<DistrictMaster> districts = districtMasterRepository.findByState_StateIdAndIsActiveTrue(stateId);
        if (districts.isEmpty()) {
            return List.of();
        }
        return districts;
    }
}