package com.example.pharmaaggregatorserver.service.master;

import com.example.pharmaaggregatorserver.entity.master.DistrictMaster;

import java.util.List;

public interface DistrictMasterService {

    // Get all districts
    public List<DistrictMaster> getAllDistricts();

    // Get districts by state ID
    public List<DistrictMaster> getDistrictsByStateId(Integer stateId);

    // Get active districts by state ID
    public List<DistrictMaster> getActiveDistrictsByStateId(Integer stateId);
}