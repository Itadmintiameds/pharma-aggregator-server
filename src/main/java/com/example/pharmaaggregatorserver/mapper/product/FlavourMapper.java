package com.example.pharmaaggregatorserver.mapper.product;

import com.example.pharmaaggregatorserver.dto.product.FlavourMasterDto;
import com.example.pharmaaggregatorserver.entity.product.Flavour;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FlavourMapper {

    public FlavourMasterDto toDto(Flavour f) {
        FlavourMasterDto dto = new FlavourMasterDto();
        dto.setFlavourId(f.getFlavourId());
        dto.setFlavourName(f.getFlavourName());
        return dto;
    }

    public List<FlavourMasterDto> toDtoList(List<Flavour> flavours) {
        return flavours.stream().map(this::toDto).toList();
    }

    public Flavour toEntity(FlavourMasterDto dto, Flavour f) {
        f.setFlavourName(dto.getFlavourName());
        return f;
    }
}
