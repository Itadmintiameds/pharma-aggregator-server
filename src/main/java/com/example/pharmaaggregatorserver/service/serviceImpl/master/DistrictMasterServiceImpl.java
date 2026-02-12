package com.example.pharmaaggregatorserver.service.serviceImpl.master;


import com.example.pharmaaggregatorserver.dto.master.ResponseDTO.DistrictResponseDTO;
import com.example.pharmaaggregatorserver.entity.master.DistrictMaster;
import com.example.pharmaaggregatorserver.repository.master.DistrictMasterRepository;
import com.example.pharmaaggregatorserver.service.master.DistrictMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DistrictMasterServiceImpl implements DistrictMasterService {

    private final DistrictMasterRepository districtMasterRepository;

    private DistrictResponseDTO convertToResponseDTO(DistrictMaster entity) {
        return new DistrictResponseDTO(
                entity.getDistrictId(),
                entity.getState() != null ? entity.getState().getStateId() : null,
                entity.getState() != null ? entity.getState().getStateName() : null,
                entity.getDistrictCode(),
                entity.getDistrictName(),
                entity.getIsActive()
        );
    }

    @Override
    public List<DistrictResponseDTO> getAllDistricts() {
        return districtMasterRepository.findAll()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<DistrictResponseDTO> getDistrictsByStateId(Long stateId) {
        return districtMasterRepository.findByStateStateId(stateId)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }
}
