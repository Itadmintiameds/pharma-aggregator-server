package com.example.pharmaaggregatorserver.mapper;

import com.example.pharmaaggregatorserver.dto.DistrictDto;
import com.example.pharmaaggregatorserver.entity.DistrictEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class DistrictMapper {

    private final StateMapper stateMapper;

    public DistrictDto toDto(DistrictEntity district) {
        DistrictDto districtDto = new DistrictDto();
        districtDto.setId(district.getId());
        districtDto.setDistrictCode(district.getDistrictCode());
        districtDto.setDistrictName(district.getDistrictName());
        districtDto.setState(stateMapper.toDto(district.getState()));
        return districtDto;
    }
}
