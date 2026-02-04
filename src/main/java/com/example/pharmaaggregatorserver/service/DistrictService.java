package com.example.pharmaaggregatorserver.service;

import com.example.pharmaaggregatorserver.dto.DistrictDto;
import com.example.pharmaaggregatorserver.entity.DistrictEntity;
import com.example.pharmaaggregatorserver.mapper.DistrictMapper;
import com.example.pharmaaggregatorserver.repository.DistrictRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class DistrictService {

    private final DistrictRepository districtRepository;
    private final DistrictMapper districtMapper;

    public List<DistrictDto> findByStateId(Long stateId) {
        List<DistrictEntity> districtEntities = districtRepository.findAllByState_Id(stateId);
        if (districtEntities.isEmpty()) {
            return List.of();
        }
        return districtEntities.stream().map(districtMapper::toDto).toList();
    }
}
