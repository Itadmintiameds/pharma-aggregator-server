package com.example.pharmaaggregatorserver.service.serviceImpl.master;

import com.example.pharmaaggregatorserver.entity.master.StateMaster;
import com.example.pharmaaggregatorserver.repository.temp.master.StateMasterRepository;
import com.example.pharmaaggregatorserver.service.master.StateMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StateMasterServiceImpl implements StateMasterService {
    private final StateMasterRepository stateMasterRepository;

    // Get all states
    @Override
    public List<StateMaster> getAllStates() {
        List<StateMaster> states = stateMasterRepository.findAll();
        if (states.isEmpty()) {
            return List.of();
        }
        return states;
    }
}