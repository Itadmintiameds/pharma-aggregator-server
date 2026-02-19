package com.example.pharmaaggregatorserver.service.product;

import com.example.pharmaaggregatorserver.dto.product.MoleculeDto;

import java.util.List;

public interface MoleculeService {
    MoleculeDto saveMolecule(MoleculeDto dto);

    MoleculeDto getMoleculeByName(String moleculeName);

    List<MoleculeDto> getAllMolecules();
}
