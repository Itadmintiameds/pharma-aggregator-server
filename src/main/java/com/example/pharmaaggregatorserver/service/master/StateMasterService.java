package com.example.pharmaaggregatorserver.service.master;

import com.example.pharmaaggregatorserver.dto.master.ResponseDTO.StateResponseDTO;
import java.util.List;

public interface StateMasterService {
    List<StateResponseDTO> getAllStates();
}