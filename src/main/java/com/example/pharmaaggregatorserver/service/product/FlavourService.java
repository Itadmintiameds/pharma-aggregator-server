package com.example.pharmaaggregatorserver.service.product;

import com.example.pharmaaggregatorserver.dto.product.FlavourMasterDto;

import java.util.List;

public interface FlavourService {

    List<FlavourMasterDto> findAll();

    FlavourMasterDto findById(Long id);

    FlavourMasterDto createFlavour(FlavourMasterDto f);

    FlavourMasterDto updateFlavour(Long flavourId, FlavourMasterDto f);

    String deleteById(Long id);
}
