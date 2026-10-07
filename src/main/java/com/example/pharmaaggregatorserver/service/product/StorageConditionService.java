package com.example.pharmaaggregatorserver.service.product;

import com.example.pharmaaggregatorserver.dto.product.StorageConditionDropdownDTO;

import java.util.List;

public interface StorageConditionService {

    List<StorageConditionDropdownDTO> getByCategoryId(Long categoryId);

    StorageConditionDropdownDTO getConditionById(Long storageConditionId);

}
