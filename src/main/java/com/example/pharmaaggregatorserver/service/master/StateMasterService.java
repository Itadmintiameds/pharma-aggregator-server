package com.example.pharmaaggregatorserver.service.master;

import com.example.pharmaaggregatorserver.entity.master.StateMaster;

import java.util.List;

public interface StateMasterService {

    // Get all states
    public List<StateMaster> getAllStates();
}