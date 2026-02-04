package com.example.pharmaaggregatorserver.mapper;

import com.example.pharmaaggregatorserver.dto.StateDto;
import com.example.pharmaaggregatorserver.entity.StateEntity;
import org.springframework.stereotype.Component;

@Component
public class StateMapper {

    public StateDto toDto(StateEntity state) {
        StateDto stateDto = new StateDto();
        stateDto.setId(state.getId());
        stateDto.setStateCode(state.getStateCode());
        stateDto.setStateName(state.getStateName());
        stateDto.setIsActive(state.getIsActive());
        return stateDto;
    }

}
