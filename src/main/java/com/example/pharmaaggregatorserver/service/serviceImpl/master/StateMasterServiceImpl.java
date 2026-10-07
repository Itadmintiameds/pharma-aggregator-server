package com.example.pharmaaggregatorserver.service.serviceImpl.master;


import com.example.pharmaaggregatorserver.dto.master.ResponseDTO.StateResponseDTO;

import com.example.pharmaaggregatorserver.entity.master.StateMaster;
import com.example.pharmaaggregatorserver.repository.master.StateMasterRepository;
import com.example.pharmaaggregatorserver.service.master.StateMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StateMasterServiceImpl implements StateMasterService {

    private final StateMasterRepository stateMasterRepository;

    private StateResponseDTO convertToResponseDTO(StateMaster entity) {
        return new StateResponseDTO(
                entity.getStateId(),
                entity.getStateCode(),
                entity.getStateName(),
                entity.getIsActive()
        );
    }

    @Override
    public List<StateResponseDTO> getAllStates() {
        return stateMasterRepository.findAll()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }
}
