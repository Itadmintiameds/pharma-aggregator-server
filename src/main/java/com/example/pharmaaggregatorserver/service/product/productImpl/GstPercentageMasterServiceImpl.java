package com.example.pharmaaggregatorserver.service.product.productImpl;

import com.example.pharmaaggregatorserver.dto.product.GstPercentageMasterDto;
import com.example.pharmaaggregatorserver.mapper.product.GstPercentageMasterMapper;
import com.example.pharmaaggregatorserver.repository.product.GstPercentageMasterRepository;
import com.example.pharmaaggregatorserver.service.product.GstPercentageMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GstPercentageMasterServiceImpl implements GstPercentageMasterService {

    private final GstPercentageMasterRepository repository;
    private final GstPercentageMasterMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<GstPercentageMasterDto> getAllGstPercentages() {
        return repository.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

}
