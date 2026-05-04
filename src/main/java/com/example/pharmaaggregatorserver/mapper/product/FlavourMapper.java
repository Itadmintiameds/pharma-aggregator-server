package com.example.pharmaaggregatorserver.mapper.product;

import com.example.pharmaaggregatorserver.dto.product.FlavourMasterDto;
import com.example.pharmaaggregatorserver.entity.product.Flavour;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class FlavourMapper {

    public FlavourMasterDto toDto(Flavour f) {
        return FlavourMasterDto
                .builder()
                .flavourId(f.getFlavourId())
                .flavourName(f.getFlavourName())
                .build();
    }

    public List<FlavourMasterDto> toDtoList(List<Flavour> flavours) {
        return flavours.stream().map(this::toDto).toList();
    }

    public Flavour toEntity(FlavourMasterDto dto, Flavour f) {
        f.setFlavourName(dto.getFlavourName());
        return f;
    }
}
