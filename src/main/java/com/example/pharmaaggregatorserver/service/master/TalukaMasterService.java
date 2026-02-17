package com.example.pharmaaggregatorserver.service.master;

import com.example.pharmaaggregatorserver.entity.master.TalukaMaster;

import java.util.List;

public interface TalukaMasterService {

    // Get all talukas
    public List<TalukaMaster> getAllTalukas();

    // Get talukas by district ID
    public List<TalukaMaster> getTalukasByDistrictId(Integer districtId);
}