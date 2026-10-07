package com.example.pharmaaggregatorserver.service.product.productImpl;

import com.example.pharmaaggregatorserver.dto.product.PackTypeDto;
import com.example.pharmaaggregatorserver.entity.product.PackType;
import com.example.pharmaaggregatorserver.repository.product.PackTypeRepository;
import com.example.pharmaaggregatorserver.service.product.PackTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PackTypeServiceImpl implements PackTypeService {

    private final PackTypeRepository packTypeRepository;

    @Override
    public PackTypeDto getPackTypeById(Long packId) {

        PackType packType = packTypeRepository.findById(packId)
                .orElseThrow(() -> new RuntimeException("PackType not found"));

        PackTypeDto dto = new PackTypeDto();
        dto.setPackId(packType.getPackId());
        dto.setPackType(packType.getPackType());

        return dto;
    }
}
