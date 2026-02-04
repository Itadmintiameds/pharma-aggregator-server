package com.example.pharmaaggregatorserver.mapper;

import com.example.pharmaaggregatorserver.dto.TalukaDto;
import com.example.pharmaaggregatorserver.entity.TalukaEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class TalukaMapper {

    private final DistrictMapper districtMapper;

    public TalukaDto toDto(TalukaEntity taluka) {
        TalukaDto talukaDto = new TalukaDto();
        talukaDto.setId(taluka.getId());
        talukaDto.setTalukaCode(taluka.getTalukaCode());
        talukaDto.setTalukaName(taluka.getTalukaName());
        talukaDto.setIsActive(taluka.getIsActive());
        talukaDto.setDistrict(districtMapper.toDto(taluka.getDistrict()));
        return talukaDto;
    }
}
