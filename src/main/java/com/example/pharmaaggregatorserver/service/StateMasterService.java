package com.example.pharmaaggregatorserver.service;


import com.example.pharmaaggregatorserver.entity.StateMaster;
import com.example.pharmaaggregatorserver.repository.StateMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StateMasterService {

    private final StateMasterRepository stateMasterRepository;

    // Get all states
    public List<StateMaster> getAllStates() {
        return stateMasterRepository.findAll();
    }
}