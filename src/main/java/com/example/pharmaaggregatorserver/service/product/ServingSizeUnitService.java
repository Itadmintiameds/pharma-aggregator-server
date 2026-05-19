package com.example.pharmaaggregatorserver.service.product;

import com.example.pharmaaggregatorserver.dto.product.ServingSizeUnitDto;

import java.util.List;

public interface ServingSizeUnitService {

    List<ServingSizeUnitDto> findAll();

    ServingSizeUnitDto findById(Long id);

    List<ServingSizeUnitDto> findByDosageFormId(Long id);

    List<ServingSizeUnitDto> findByProductFormId(Long id);
}
