package com.example.pharmaaggregatorserver.service;

import com.example.pharmaaggregatorserver.dto.TalukaDto;
import com.example.pharmaaggregatorserver.entity.TalukaEntity;
import com.example.pharmaaggregatorserver.mapper.TalukaMapper;
import com.example.pharmaaggregatorserver.repository.TalukaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class TalukaService {

    private final TalukaRepository talukaRepository;
    private final TalukaMapper talukaMapper;

    public List<TalukaDto> findByDistrictId(Long districtId) {
        List<TalukaEntity> talukaEntities = talukaRepository.findAllByDistrict_Id(districtId);
        if (talukaEntities.isEmpty()) {
            return List.of();
        }
        return talukaEntities.stream().map(talukaMapper::toDto).toList();
    }
}
