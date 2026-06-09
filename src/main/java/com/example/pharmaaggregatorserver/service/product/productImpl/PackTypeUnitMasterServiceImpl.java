package com.example.pharmaaggregatorserver.service.product.productImpl;

import com.example.pharmaaggregatorserver.dto.product.PackTypeUnitMasterDto;
import com.example.pharmaaggregatorserver.mapper.product.PackTypeUnitMasterMapper;
import com.example.pharmaaggregatorserver.repository.product.PackTypeUnitMasterRepository;
import com.example.pharmaaggregatorserver.service.product.PackTypeUnitMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PackTypeUnitMasterServiceImpl implements PackTypeUnitMasterService {

    private final PackTypeUnitMasterRepository repository;
    private final PackTypeUnitMasterMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<PackTypeUnitMasterDto> getAllUnits() {
        return repository.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

}

