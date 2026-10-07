package com.example.pharmaaggregatorserver.service.product;

import com.example.pharmaaggregatorserver.dto.product.MoleculeStrengthFormatDto;

import java.util.List;

public interface MoleculeStrengthFormatService {

    List<MoleculeStrengthFormatDto> getByDosageId(Long dosageId);

}
