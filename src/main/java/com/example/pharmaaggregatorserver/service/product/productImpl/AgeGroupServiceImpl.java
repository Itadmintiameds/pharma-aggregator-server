package com.example.pharmaaggregatorserver.service.product.productImpl;

import com.example.pharmaaggregatorserver.dto.product.AgeGroupMasterDto;
import com.example.pharmaaggregatorserver.mapper.product.AgeGroupMapper;
import com.example.pharmaaggregatorserver.repository.product.AgeGroupMasterRepository;
import com.example.pharmaaggregatorserver.service.product.AgeGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AgeGroupServiceImpl implements AgeGroupService {

    private final AgeGroupMasterRepository repository;
    private final AgeGroupMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<AgeGroupMasterDto> getAllAgeGroups() {

        return repository.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

}