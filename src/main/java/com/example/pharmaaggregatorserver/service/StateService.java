package com.example.pharmaaggregatorserver.service;

import com.example.pharmaaggregatorserver.dto.StateDto;
import com.example.pharmaaggregatorserver.entity.StateEntity;
import com.example.pharmaaggregatorserver.mapper.StateMapper;
import com.example.pharmaaggregatorserver.repository.StateRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class StateService {

    private final StateRepository stateRepository;
    private final StateMapper stateMapper;

    public List<StateDto> findAll() {
        List<StateEntity> allStates = stateRepository.findAll();
        if (allStates.isEmpty()) {
            return List.of();
        }
        return allStates.stream().map(stateMapper::toDto).toList();
    }
}
