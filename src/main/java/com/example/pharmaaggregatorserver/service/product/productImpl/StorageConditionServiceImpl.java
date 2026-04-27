package com.example.pharmaaggregatorserver.service.product.productImpl;

import com.example.pharmaaggregatorserver.dto.product.StorageConditionResponseDTO;
import com.example.pharmaaggregatorserver.repository.product.StorageConditionMasterRepository;
import com.example.pharmaaggregatorserver.service.product.StorageConditionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.pharmaaggregatorserver.dto.product.StorageConditionDropdownDTO;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StorageConditionServiceImpl implements StorageConditionService {

    private final StorageConditionMasterRepository repository;

    @Override
    public List<StorageConditionDropdownDTO> getByCategoryId(Long categoryId) {
        return repository.findByCategoryId(categoryId);
    }
}


